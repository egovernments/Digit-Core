'use strict';

// The finding log line of ValidationAuditLogger, with the same fields, order and sanitizing:
//   request_validation mode=<MODE> code=<CODE> rule=<rule> location=<location> kind=<kind> handler=<handler>
//     method=<METHOD> length=<n>[ key=value ...]
// REPORT lines are sampled; ENFORCE lines never are. No input value, decoded variant or exception message is logged.

var MAX_SAFE_TEXT = 256;

/** At most 256 UTF-16 units; any unit outside 0x20-0x7E, and '<' and '>', becomes '_'. null/undefined give "". */
function safeText(value) {
  if (value === null || value === undefined) {
    return '';
  }
  var text = typeof value === 'string' ? value : String(value);
  var n = Math.min(text.length, MAX_SAFE_TEXT);
  var out = '';
  for (var i = 0; i < n; i++) {
    var c = text.charCodeAt(i);
    out += c >= 32 && c <= 126 && c !== 0x3C && c !== 0x3E ? text.charAt(i) : '_';
  }
  return out;
}

/** Calls a logger method without letting a logging failure affect the request. */
function emit(logger, level, line) {
  var fn = logger && logger[level];
  if (typeof fn !== 'function') {
    return;
  }
  try {
    fn.call(logger, line);
  } catch (e) {
    // logging must never change a decision
  }
}

function defaultLogger() {
  return {
    warn: function (line) {
      console.warn(line); // eslint-disable-line no-console
    },
    info: function (line) {
      console.info(line); // eslint-disable-line no-console
    },
    error: function (line) {
      console.error(line); // eslint-disable-line no-console
    }
  };
}

/**
 * @param {object} logger { warn(line), info?(line), error?(line) }
 * @param {number} reportSampleRate 0..1
 * @param {function} [logContext] (req) => { key: value } appended to finding lines
 * @param {function} [random] () => number in [0, 1), default Math.random
 */
function AuditLogger(logger, reportSampleRate, logContext, random) {
  this.logger = logger;
  this.rate = reportSampleRate;
  this.logContext = typeof logContext === 'function' ? logContext : null;
  this.random = typeof random === 'function' ? random : Math.random;
}

// " key=value" pairs from options.logContext, computed once per request (undefined values are left out).
AuditLogger.prototype.contextSuffix = function (ctx) {
  if (this.logContext === null || !ctx || !ctx.req) {
    return '';
  }
  var state = ctx.state;
  if (state && typeof state.logContext === 'string') {
    return state.logContext;
  }
  var suffix = '';
  try {
    var values = this.logContext(ctx.req);
    if (values !== null && typeof values === 'object') {
      Object.keys(values).forEach(function (key) {
        var value = values[key];
        if (value !== undefined) {
          suffix += ' ' + safeText(key) + '=' + safeText(value === null ? '' : String(value));
        }
      });
    }
  } catch (e) {
    suffix = '';
  }
  if (state) {
    state.logContext = suffix;
  }
  return suffix;
};

/** Logs one finding (WARN). ctx: { req, state, handler, method }. */
AuditLogger.prototype.violation = function (mode, violation, kind, ctx) {
  if (mode === 'REPORT' && this.random() >= this.rate) {
    return;
  }
  var line = 'request_validation mode=' + mode + ' code=' + violation.code + ' rule=' + safeText(violation.ruleId)
      + ' location=' + safeText(violation.location) + ' kind=' + kind + ' handler=' + safeText(ctx ? ctx.handler : '')
      + ' method=' + safeText(ctx ? ctx.method : '') + ' length=' + violation.length + this.contextSuffix(ctx);
  emit(this.logger, 'warn', line);
};

AuditLogger.prototype.warn = function (line) {
  emit(this.logger, 'warn', line);
};

AuditLogger.prototype.info = function (line) {
  emit(this.logger, 'info', line);
};

/** ERROR when the logger has error(), else WARN. */
AuditLogger.prototype.error = function (line) {
  emit(this.logger, typeof this.logger.error === 'function' ? 'error' : 'warn', line);
};

module.exports = {
  AuditLogger: AuditLogger,
  safeText: safeText,
  defaultLogger: defaultLogger,
  emit: emit
};
