'use strict';

// Public API: createRequestValidation(options) and the error helpers. With the package disabled (the default) the
// factory validates nothing and returns no-op middlewares of the same arity.

var ViolationCode = require('./core/ViolationCode');
var Violation = require('./core/Violation');
var InspectionError = require('./core/InspectionError');
var InspectionLimits = require('./core/InspectionLimits');
var ContentPolicy = require('./core/ContentPolicy');
var ContentDetector = require('./core/ContentDetector');
var SkipPathMatcher = require('./core/SkipPathMatcher');
var SafeLocationFormatter = require('./core/SafeLocationFormatter');
var JsonDocumentInspector = require('./core/JsonDocumentInspector');
var resolve = require('./config/resolve');
var defaults = require('./config/defaults');
var coverage = require('./config/coverage');
var auditLogger = require('./web/auditLogger');
var Reporter = require('./web/reporter');
var requestState = require('./web/requestState');
var adapter = require('./web/adapter');
var errorResponse = require('./web/errorResponse');
var pkg = require('../package.json');

var conformsTo = pkg.egovRequestValidation.conformsTo;
var CONFORMS_TO = Object.freeze({
  jarSha256: conformsTo.jarSha256,
  jsoup: conformsTo.jsoup,
  jacksonCore: conformsTo.jacksonCore,
  javaCharacterData: conformsTo.javaCharacterData
});

var defaultDetector = null;

function javaDefaultDetector() {
  if (defaultDetector === null) {
    defaultDetector = new ContentDetector(ContentPolicy.defaults());
  }
  return defaultDetector;
}

function isPlainObject(value) {
  return value !== null && typeof value === 'object' && !Array.isArray(value);
}

// detect() and inspectJson() of an instance: never log, never throw for findings.
function inspectionFunctions(detector, limits, rejectDuplicateKeys, rejectDualRequestInfo) {
  var inspector = new JsonDocumentInspector(detector);
  function detect(value) {
    return detector.detect(value);
  }
  function inspectJson(body, options) {
    var opts = options === undefined || options === null ? {} : options;
    if (!isPlainObject(opts)) {
      throw new TypeError('options must be an object');
    }
    var useDetector = detector;
    var useInspector = inspector;
    if (opts.rules !== undefined && opts.rules !== null) {
      if (!isPlainObject(opts.rules)) {
        throw new TypeError('options.rules must be an object');
      }
      var merged = detector.policy.toJSON();
      Object.keys(opts.rules).forEach(function (key) {
        if (opts.rules[key] !== undefined) {
          merged[key] = opts.rules[key];
        }
      });
      useDetector = new ContentDetector(new ContentPolicy(merged));
      useInspector = new JsonDocumentInspector(useDetector);
    }
    var useLimits = opts.limits === undefined || opts.limits === null ? limits : InspectionLimits.fromObject(opts.limits, limits);
    var matcher = new SkipPathMatcher(opts.skipPaths === undefined || opts.skipPaths === null ? [] : opts.skipPaths);
    var dup = opts.rejectDuplicateKeys === undefined ? rejectDuplicateKeys : opts.rejectDuplicateKeys;
    var dual = opts.rejectDualRequestInfo === undefined ? rejectDualRequestInfo : opts.rejectDualRequestInfo;
    var findings = [];
    var failure = null;
    try {
      useInspector.inspect(body, useLimits, matcher, dup, dual, function (violation) {
        findings.push(violation);
      });
    } catch (e) {
      if (!(e instanceof InspectionError)) {
        throw e;
      }
      failure = e.violation;
    }
    return { findings: findings, failure: failure };
  }
  return { detect: detect, inspectJson: inspectJson };
}

function freezeInstance(instance) {
  Object.keys(instance).forEach(function (key) {
    Object.defineProperty(instance, key, { value: instance[key], enumerable: true, writable: false, configurable: false });
  });
  return instance;
}

/**
 * Creates the request-validation middlewares for one service.
 * Configuration precedence: EGOV_REQUEST_VALIDATION_* environment variables, then `options`, then the Java defaults.
 * Disabled unless EGOV_REQUEST_VALIDATION_ENABLED (or options.enabled) is true. When enabled, an invalid setting
 * throws (the service does not start); startup lines describe the effective configuration.
 */
function createRequestValidation(options) {
  var opts = options === undefined || options === null ? {} : options;
  if (!resolve.isEnabled(isPlainObject(opts) ? opts : {})) {
    var off = adapter.createDisabledAdapter();
    var fns = inspectionFunctions(javaDefaultDetector(), InspectionLimits.defaults(), true, true);
    return freezeInstance({
      enabled: false,
      config: defaults.defaultConfig(),
      beforeParsers: off.beforeParsers,
      jsonVerify: off.jsonVerify,
      formVerify: off.formVerify,
      afterParsers: Object.freeze(off.afterParsers),
      pathParams: off.pathParams,
      multipartFields: off.multipartFields,
      inspectJsonBuffer: off.inspectJsonBuffer,
      detect: fns.detect,
      inspectJson: fns.inspectJson
    });
  }

  var resolved = resolve.resolveConfig(opts);
  var cfg = resolved.internal;
  var logger = opts.logger === undefined || opts.logger === null ? auditLogger.defaultLogger() : opts.logger;
  var audit = new auditLogger.AuditLogger(logger, cfg.reportSampleRate, opts.logContext);
  var detector = new ContentDetector(cfg.contentPolicy);
  var inspector = new JsonDocumentInspector(detector);
  var store = new requestState.StateStore(function (method, pathname) {
    return resolved.resolver.resolve(method, pathname);
  });
  var web = adapter.createAdapter({
    cfg: cfg,
    store: store,
    detector: detector,
    inspector: inspector,
    reporter: new Reporter(audit),
    audit: audit,
    respond: opts.respond
  });
  coverage.startupLines(resolved).forEach(function (entry) {
    if (entry.level === 'warn') {
      audit.warn(entry.line);
    } else {
      audit.info(entry.line);
    }
  });
  var fns2 = inspectionFunctions(detector, cfg.limitsObject, cfg.rejectDuplicateKeys, cfg.rejectDualRequestInfo);
  return freezeInstance({
    enabled: true,
    config: resolved.effective,
    beforeParsers: web.beforeParsers,
    jsonVerify: web.jsonVerify,
    formVerify: web.formVerify,
    afterParsers: Object.freeze(web.afterParsers),
    pathParams: web.pathParams,
    multipartFields: web.multipartFields,
    inspectJsonBuffer: web.inspectJsonBuffer,
    detect: fns2.detect,
    inspectJson: fns2.inspectJson
  });
}

module.exports = {
  createRequestValidation: createRequestValidation,
  RequestValidationError: errorResponse.RequestValidationError,
  isRequestValidationError: errorResponse.isRequestValidationError,
  VIOLATION_MESSAGES: ViolationCode.VIOLATION_MESSAGES,
  CONFORMS_TO: CONFORMS_TO,
  ViolationCode: ViolationCode,
  Violation: Violation,
  InspectionError: InspectionError,
  InspectionLimits: InspectionLimits,
  ContentPolicy: ContentPolicy,
  ContentDetector: ContentDetector,
  SkipPathMatcher: SkipPathMatcher,
  SafeLocationFormatter: SafeLocationFormatter,
  JsonDocumentInspector: JsonDocumentInspector
};
