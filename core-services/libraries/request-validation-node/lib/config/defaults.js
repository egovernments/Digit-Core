'use strict';

// The settings, their environment variables, types and defaults. The first 23 are the Java library's settings
// (egov.request-validation.*) with the same names and defaults; the last 3 exist only in the Node package.

var InspectionLimits = require('../core/InspectionLimits');
var ContentPolicy = require('../core/ContentPolicy');

var JAR_SHA256 = 'c3b55d47f25d4b027d0109c25dd61b30dda78339a3cb627613ce07004c7e85b4';

var MODES = Object.freeze(['REPORT', 'ENFORCE']);
var ACTIVATIONS = Object.freeze(['ANNOTATED', 'ALL']);

function setting(key, property, path, type, defaultValue, extra) {
  var env = 'EGOV_REQUEST_VALIDATION_' + property.replace(/[.-]/g, '_').toUpperCase();
  var entry = { key: key, property: 'egov.request-validation.' + property, env: env, path: path, type: type,
    defaultValue: defaultValue };
  if (extra) {
    Object.keys(extra).forEach(function (name) {
      entry[name] = extra[name];
    });
  }
  return Object.freeze(entry);
}

var L = InspectionLimits.DEFAULTS;
var R = ContentPolicy.DEFAULTS;

// type: boolean | enum | double | int | long | intList | stringList. `path` is where the value sits in the options.
var SETTINGS = Object.freeze([
  setting('enabled', 'enabled', ['enabled'], 'enabled', false),
  setting('structuredDefault', 'structured-default', ['structuredDefault'], 'boolean', null),
  setting('activation', 'activation', ['activation'], 'enum', 'ANNOTATED', { values: ['ANNOTATED', 'ALL'] }),
  setting('mode', 'mode', ['mode'], 'enum', 'REPORT', { values: ['DEFAULT', 'REPORT', 'ENFORCE'] }),
  setting('inspectContentTypeHeader', 'inspect-content-type-header', ['inspectContentTypeHeader'], 'boolean', true),
  setting('rejectDuplicateKeys', 'reject-duplicate-keys', ['rejectDuplicateKeys'], 'boolean', true),
  setting('rejectDualRequestInfo', 'reject-dual-request-info', ['rejectDualRequestInfo'], 'boolean', true),
  setting('reportSampleRate', 'log.report-sample-rate', ['log', 'reportSampleRate'], 'double', 1.0),
  setting('maxBodyBytes', 'limits.max-body-bytes', ['limits', 'maxBodyBytes'], 'int', L.maxBodyBytes),
  setting('maxDepth', 'limits.max-depth', ['limits', 'maxDepth'], 'int', L.maxDepth),
  setting('maxStringLength', 'limits.max-string-length', ['limits', 'maxStringLength'], 'int', L.maxStringLength),
  setting('maxNameLength', 'limits.max-name-length', ['limits', 'maxNameLength'], 'int', L.maxNameLength),
  setting('maxTokens', 'limits.max-tokens', ['limits', 'maxTokens'], 'long', L.maxTokens),
  setting('maxNumberLength', 'limits.max-number-length', ['limits', 'maxNumberLength'], 'int', L.maxNumberLength),
  setting('maxScalarLength', 'limits.max-scalar-length', ['limits', 'maxScalarLength'], 'int', L.maxScalarLength),
  setting('markupStart', 'rules.markup-start', ['rules', 'markupStart'], 'boolean', R.markupStart),
  setting('urlScheme', 'rules.url-scheme', ['rules', 'urlScheme'], 'boolean', R.urlScheme),
  setting('eventHandler', 'rules.event-handler', ['rules', 'eventHandler'], 'boolean', R.eventHandler),
  setting('disallowedControls', 'rules.disallowed-controls', ['rules', 'disallowedControls'], 'intList',
      R.disallowedControls),
  setting('deniedSchemes', 'rules.denied-schemes', ['rules', 'deniedSchemes'], 'stringList', R.deniedSchemes),
  setting('deniedDataMediaTypes', 'rules.denied-data-media-types', ['rules', 'deniedDataMediaTypes'], 'stringList',
      R.deniedDataMediaTypes),
  setting('decodeRounds', 'rules.decode-rounds', ['rules', 'decodeRounds'], 'int', R.decodeRounds),
  setting('normalizeNfkc', 'rules.normalize-nfkc', ['rules', 'normalizeNfkc'], 'boolean', R.normalizeNfkc),
  // Node-only settings
  setting('excludePaths', 'node.exclude-paths', ['excludePaths'], 'excludeList', Object.freeze([]), { nodeOnly: true }),
  setting('maxInspectionMillis', 'node.max-inspection-millis', ['maxInspectionMillis'], 'int', null,
      { nodeOnly: true, min: 1 }),
  setting('slowInspectionWarnMillis', 'node.slow-inspection-warn-millis', ['slowInspectionWarnMillis'], 'int', 250,
      { nodeOnly: true, min: 0 })
]);

var BY_KEY = {};
SETTINGS.forEach(function (s) {
  BY_KEY[s.key] = s;
});

var LIMIT_KEYS = Object.freeze(InspectionLimits.FIELDS.slice());
var RULE_KEYS = Object.freeze(ContentPolicy.KEYS.slice());

// Option keys accepted at each level of the options object (anything else fails startup, as Java's
// ignoreUnknownFields = false does for its properties).
var OPTION_KEYS = Object.freeze([
  'enabled', 'structuredDefault', 'activation', 'mode', 'inspectContentTypeHeader', 'rejectDuplicateKeys',
  'rejectDualRequestInfo', 'log', 'limits', 'rules', 'routes', 'excludePaths', 'maxInspectionMillis',
  'slowInspectionWarnMillis', 'logger', 'logContext', 'respond', 'env'
]);
var LOG_KEYS = Object.freeze(['reportSampleRate']);
var ROUTE_KEYS = Object.freeze(['path', 'prefix', 'method', 'name', 'enabled', 'structured', 'mode', 'skipPaths',
  'limits', 'reason', 'exemption']);
var EXCLUDE_KEYS = Object.freeze(['path', 'reason']);

/** The Java defaults as an EffectiveConfig-shaped object (used when the package is disabled). */
function defaultConfig() {
  var limits = {};
  LIMIT_KEYS.forEach(function (k) {
    limits[k] = L[k];
  });
  var rules = {};
  RULE_KEYS.forEach(function (k) {
    var v = R[k];
    rules[k] = Array.isArray(v) ? Object.freeze(v.slice()) : v;
  });
  return Object.freeze({
    enabled: false,
    structuredDefault: null,
    activation: 'ANNOTATED',
    mode: 'REPORT',
    inspectContentTypeHeader: true,
    rejectDuplicateKeys: true,
    rejectDualRequestInfo: true,
    reportSampleRate: 1,
    limits: Object.freeze(limits),
    rules: Object.freeze(rules),
    routes: Object.freeze([]),
    excludePaths: Object.freeze([]),
    maxInspectionMillis: null,
    slowInspectionWarnMillis: 250
  });
}

module.exports = {
  JAR_SHA256: JAR_SHA256,
  MODES: MODES,
  ACTIVATIONS: ACTIVATIONS,
  SETTINGS: SETTINGS,
  BY_KEY: BY_KEY,
  LIMIT_KEYS: LIMIT_KEYS,
  RULE_KEYS: RULE_KEYS,
  OPTION_KEYS: OPTION_KEYS,
  LOG_KEYS: LOG_KEYS,
  ROUTE_KEYS: ROUTE_KEYS,
  EXCLUDE_KEYS: EXCLUDE_KEYS,
  defaultConfig: defaultConfig
};
