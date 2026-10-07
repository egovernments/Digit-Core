'use strict';

// Port of JsonDocumentInspector: streaming inspection of a JSON body without building objects. Property names and
// string values go through the ContentDetector; numbers, booleans and null never do. Content findings are reported
// to the consumer in document order; a structural or limit finding stops inspection with an InspectionError. The
// token stream comes from a Jackson-equivalent tokenizer (jackson/Tokenizer.js), so the same bytes give the same
// ordered findings, the same failure, at the same location, as the Java library.

var ViolationCode = require('./ViolationCode');
var Violation = require('./Violation');
var InspectionError = require('./InspectionError');
var InspectionLimits = require('./InspectionLimits');
var SkipPathMatcher = require('./SkipPathMatcher');
var SafeLocationFormatter = require('./SafeLocationFormatter');
var FlaggedValue = require('./FlaggedValue');
var utf8 = require('./jdk/utf8');
var Tokenizer = require('./jackson/Tokenizer');

var JsonParseError = Tokenizer.JsonParseError;
var StreamConstraintsError = Tokenizer.StreamConstraintsError;

var INT_MAX = 2147483647;
var BUDGET_TOKENS = 256;          // maxInspectionMillis: clock check interval in tokens
var BUDGET_LONG_TEXT = 4096;      // and after every name or string longer than this

function failure(code, rule, path, length) {
  return new InspectionError(new Violation(code, rule, SafeLocationFormatter.format(path), Math.max(0, length)));
}

function Context(object, hasSegment) {
  this.object = object;
  this.hasSegment = hasSegment;
  this.names = object ? new Set() : null;
  this.nextIndex = 0;
  this.currentField = null;
  this.upperRequestInfo = false;
  this.lowerRequestInfo = false;
}

function hasUtf8Bom(body) {
  return body.length >= 3 && body[0] === 0xEF && body[1] === 0xBB && body[2] === 0xBF;
}

function toBuffer(body) {
  if (Buffer.isBuffer(body)) {
    return body;
  }
  if (body instanceof Uint8Array) {
    return Buffer.from(body.buffer, body.byteOffset, body.byteLength);
  }
  throw new TypeError('body must be a Buffer or Uint8Array');
}

/** Drops the previous token's pending segment; always returns false so callers can reset the flag. */
function settle(path, pending) {
  if (pending) {
    path.pop();
  }
  return false;
}

function nextSegment(parent, path) {
  if (parent.object) {
    if (parent.currentField === null) {
      throw failure(ViolationCode.REQUEST_JSON_MALFORMED, 'json', path, 0);
    }
    return parent.currentField;
  }
  return String(parent.nextIndex++);
}

class JsonDocumentInspector {
  /** @param {ContentDetector} detector the detector applied to names and string values */
  constructor(detector) {
    if (detector === null || detector === undefined || typeof detector.detect !== 'function') {
      throw new TypeError('detector');
    }
    Object.defineProperty(this, 'detector', { value: detector, enumerable: true });
    Object.freeze(this);
  }

  /**
   * Inspects one JSON body.
   * @param {Buffer|Uint8Array} body the raw request body bytes (UTF-8, an optional leading BOM is skipped)
   * @param {InspectionLimits} limits
   * @param {SkipPathMatcher} skipPaths subtrees excluded from content detection (structural checks still apply)
   * @param {boolean} rejectDuplicateKeys a repeated name within one object is REQUEST_JSON_DUPLICATE_KEY
   * @param {boolean} rejectDualRequestInfo both "RequestInfo" and "requestInfo" at the top level is malformed
   * @param {function(Violation)} contentViolations called for each content finding, in document order; whatever it
   *   throws propagates unchanged
   * @param {object} [options] Node-only, not part of the Java signature
   * @param {number} [options.deadline] stop with REQUEST_LIMIT_EXCEEDED/inspection-budget once options.now() passes
   *   this value (checked every 256 tokens and after every name or string longer than 4,096 units)
   * @param {function(): number} [options.now] clock for the deadline (default Date.now)
   * @param {{size: number}} [options.textBufferPool] the recycled parser text buffer to start from and return to
   *   (default: none, as on a thread that has not parsed before)
   * @param {function(FlaggedValue): boolean} [options.exemption] the Java overload's exemption: offered each flagged
   *   string value once the whole body has passed the syntax and limit checks; a finding it returns true for is
   *   dropped, false or a throw keeps it. Findings are still reported in document order; if the body fails a syntax
   *   or limit check, every finding before the failure is reported unchanged and the exemption is not consulted.
   * @throws {InspectionError} the structural or limit finding
   */
  inspect(body, limits, skipPaths, rejectDuplicateKeys, rejectDualRequestInfo, contentViolations, options) {
    if (body === null || body === undefined) {
      throw new TypeError('body');
    }
    if (!(limits instanceof InspectionLimits)) {
      throw new TypeError('limits');
    }
    if (!(skipPaths instanceof SkipPathMatcher)) {
      throw new TypeError('skipPaths');
    }
    if (typeof rejectDuplicateKeys !== 'boolean') {
      throw new TypeError('rejectDuplicateKeys');
    }
    if (typeof rejectDualRequestInfo !== 'boolean') {
      throw new TypeError('rejectDualRequestInfo');
    }
    if (typeof contentViolations !== 'function') {
      throw new TypeError('contentViolations');
    }
    var bytes = toBuffer(body);
    var opts = options === undefined || options === null ? {} : options;
    var deadline = typeof opts.deadline === 'number' ? opts.deadline : null;
    var now = typeof opts.now === 'function' ? opts.now : Date.now;
    if (opts.exemption !== undefined && opts.exemption !== null && typeof opts.exemption !== 'function') {
      throw new TypeError('exemption');
    }
    var exemption = typeof opts.exemption === 'function' ? opts.exemption : null;
    // Findings held back until the whole body has been read, and every string value by JSON Pointer.
    var deferred = exemption === null ? null : { strings: new Map(), findings: [] };

    if (bytes.length > limits.maxBodyBytes) {
      throw failure(ViolationCode.REQUEST_LIMIT_EXCEEDED, 'max-body-bytes', [], bytes.length);
    }
    if (bytes.length === 0) {
      throw failure(ViolationCode.REQUEST_JSON_MALFORMED, 'json', [], 0);
    }

    var parser = new Tokenizer(utf8.readerView(bytes, hasUtf8Bom(bytes) ? 3 : 0), {
      maxNestingDepth: limits.maxDepth,
      maxStringLength: limits.maxStringLength,
      maxNumberLength: limits.maxNumberLength,
      textBufferPool: opts.textBufferPool
    });

    var detector = this.detector;
    var contexts = [];
    // Raw segments of the innermost open container, plus the last name or value (or an ended container) until the
    // next token moves past it, so a failure while reading the next token names the last token's location.
    var path = [];
    var pending = false;
    var started = false;
    var complete = false;
    var tokenCount = 0;
    var consumerFailed = false;
    var countdown = BUDGET_TOKENS;
    var longText = false;

    function inspectContent(value, stringValue) {
      if (deferred !== null && stringValue) {
        deferred.strings.set(FlaggedValue.format(path), value);
      }
      if (skipPaths.matches(path)) {
        return;
      }
      var rule = detector.detect(value);
      if (rule !== null && rule !== undefined) {
        var violation = new Violation(ViolationCode.REQUEST_CONTENT_NOT_ALLOWED, rule,
            SafeLocationFormatter.format(path), value.length);
        if (deferred !== null) {
          // Field names are never offered to the exemption.
          deferred.findings.push({ violation: violation, path: stringValue ? path.slice() : null, value: value });
          return;
        }
        consumerFailed = true;
        contentViolations(violation);
        consumerFailed = false;
      }
    }

    function allows(finding) {
      try {
        return exemption(new FlaggedValue(finding.path, finding.value, finding.violation.ruleId,
            deferred.strings)) === true;
      } catch (e) {
        // Fails closed: a broken exemption keeps the finding (REPORT logs it, ENFORCE rejects).
        return false;
      }
    }

    function report(consult) {
      var findings = deferred.findings;
      deferred.findings = [];
      for (var i = 0; i < findings.length; i++) {
        if (consult && findings[i].path !== null && allows(findings[i])) {
          continue;
        }
        contentViolations(findings[i].violation);
      }
    }

    // A syntax or limit failure first reports the held-back findings unchanged, as without an exemption.
    function failed(error) {
      if (deferred !== null && error instanceof InspectionError) {
        report(false);
      }
      return error;
    }

    try {
      for (;;) {
        if (deadline !== null && (--countdown === 0 || longText)) {
          countdown = BUDGET_TOKENS;
          longText = false;
          if (now() > deadline) {
            throw failure(ViolationCode.REQUEST_LIMIT_EXCEEDED, 'inspection-budget', path, 0);
          }
        }
        var token = parser.nextToken();
        if (token === null) {
          break;
        }
        tokenCount++;
        if (tokenCount > limits.maxTokens) {
          throw failure(ViolationCode.REQUEST_LIMIT_EXCEEDED, 'max-tokens', path,
              tokenCount > INT_MAX ? INT_MAX : tokenCount);
        }
        if (complete) {
          throw failure(ViolationCode.REQUEST_JSON_MALFORMED, 'trailing-content', path, 0);
        }
        started = true;

        if (token === Tokenizer.FIELD_NAME) {
          var object = contexts.length === 0 ? null : contexts[contexts.length - 1];
          if (object === null || !object.object) {
            throw failure(ViolationCode.REQUEST_JSON_MALFORMED, 'json', path, 0);
          }
          pending = settle(path, pending);
          var name = parser.getCurrentName();
          path.push(name);
          if (name.length > limits.maxNameLength) {
            throw failure(ViolationCode.REQUEST_LIMIT_EXCEEDED, 'max-name-length', path, name.length);
          }
          if (rejectDuplicateKeys) {
            if (object.names.has(name)) {
              throw failure(ViolationCode.REQUEST_JSON_DUPLICATE_KEY, 'duplicate-key', path, name.length);
            }
            object.names.add(name);
          }
          if (rejectDualRequestInfo && contexts.length === 1) {
            if (name === 'RequestInfo') {
              object.upperRequestInfo = true;
            } else if (name === 'requestInfo') {
              object.lowerRequestInfo = true;
            }
            if (object.upperRequestInfo && object.lowerRequestInfo) {
              throw failure(ViolationCode.REQUEST_JSON_MALFORMED, 'dual-request-info', path, name.length);
            }
          }
          object.currentField = name;
          inspectContent(name, false);
          if (name.length > BUDGET_LONG_TEXT) {
            longText = true;
          }
          pending = true;
          continue;
        }

        if (token === Tokenizer.END_OBJECT || token === Tokenizer.END_ARRAY) {
          if (contexts.length === 0) {
            throw failure(ViolationCode.REQUEST_JSON_MALFORMED, 'json', path, 0);
          }
          var ended = contexts.pop();
          if ((token === Tokenizer.END_OBJECT) !== ended.object) {
            throw failure(ViolationCode.REQUEST_JSON_MALFORMED, 'json', path, 0);
          }
          pending = settle(path, pending);
          // The path now names the ended container; its own segment goes when the next token arrives.
          pending = ended.hasSegment;
          if (contexts.length === 0) {
            complete = true;
          }
          continue;
        }

        pending = settle(path, pending);
        var parent = contexts.length === 0 ? null : contexts[contexts.length - 1];
        if (parent !== null) {
          path.push(nextSegment(parent, path));
        }
        if (token === Tokenizer.START_OBJECT || token === Tokenizer.START_ARRAY) {
          var depth = contexts.length + 1;
          if (depth > limits.maxDepth) {
            throw failure(ViolationCode.REQUEST_LIMIT_EXCEEDED, 'max-depth', path, depth);
          }
          // The container's segment stays on the path until its matching end token.
          contexts.push(new Context(token === Tokenizer.START_OBJECT, parent !== null));
          continue;
        }

        if (token < Tokenizer.VALUE_STRING) {
          throw failure(ViolationCode.REQUEST_JSON_MALFORMED, 'json', path, 0); // not a scalar value
        }
        if (token === Tokenizer.VALUE_STRING) {
          var value = parser.getText();
          if (value.length > limits.maxStringLength) {
            throw failure(ViolationCode.REQUEST_LIMIT_EXCEEDED, 'max-string-length', path, value.length);
          }
          inspectContent(value, true);
          if (value.length > BUDGET_LONG_TEXT) {
            longText = true;
          }
        } else if (token === Tokenizer.VALUE_NUMBER_INT || token === Tokenizer.VALUE_NUMBER_FLOAT) {
          var length = parser.getTextLength();
          if (length > limits.maxNumberLength) {
            throw failure(ViolationCode.REQUEST_LIMIT_EXCEEDED, 'max-number-length', path, length);
          }
        }
        pending = parent !== null;
        if (contexts.length === 0) {
          complete = true;
        }
      }
    } catch (e) {
      if (consumerFailed || e instanceof InspectionError) {
        throw failed(e);
      }
      if (e instanceof StreamConstraintsError) {
        throw failed(failure(ViolationCode.REQUEST_LIMIT_EXCEEDED, 'parser-limit', path, 0));
      }
      if (e instanceof JsonParseError) {
        throw failed(failure(ViolationCode.REQUEST_JSON_MALFORMED, 'json', path, 0));
      }
      throw e;
    } finally {
      parser.close();
    }

    if (!started || !complete || contexts.length !== 0) {
      throw failed(failure(ViolationCode.REQUEST_JSON_MALFORMED, 'json', path, 0));
    }
    if (deferred !== null) {
      report(true);
    }
  }
}

module.exports = JsonDocumentInspector;
