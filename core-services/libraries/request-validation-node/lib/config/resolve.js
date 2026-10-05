'use strict';

// Effective configuration: environment variables, then code options, then the Java defaults. When the package is
// enabled every value is validated and an invalid one throws (the service does not start, as in Java). Routes play
// the role of @ValidateRequest: a prefix entry is the class-level declaration, an exact entry the method-level one.

var conv = require('./env');
var defaults = require('./defaults');
var InspectionLimits = require('../core/InspectionLimits');
var ContentPolicy = require('../core/ContentPolicy');
var SkipPathMatcher = require('../core/SkipPathMatcher');
var nfkc = require('../core/jdk/nfkc');
var javaChars = require('../core/jdk/javaChars');

var PREFIX = 'request-validation: ';
var MEMO_LIMIT = 10000;

function configError(message) {
  return new RangeError(PREFIX + message);
}

function typeError(message) {
  return new TypeError(PREFIX + message);
}

function isPlainObject(value) {
  return value !== null && typeof value === 'object' && !Array.isArray(value);
}

function exactLookup(env) {
  return function (name) {
    if (!env || !Object.prototype.hasOwnProperty.call(env, name)) {
      return null;
    }
    var value = env[name];
    return value === undefined || value === null ? null : String(value);
  };
}

/** The environment object to read: options.env, process.env, or none when options.env is false. */
function environmentOf(options) {
  if (options && options.env === false) {
    return null;
  }
  if (options && options.env !== undefined && options.env !== null) {
    return options.env;
  }
  return process.env;
}

/**
 * Whether the package is on. Mirrors Spring's @ConditionalOnProperty(havingValue = "true"): the environment
 * variable, when present, decides, and only the text "true" (any case) enables; otherwise options.enabled must be
 * true. Never validates anything else.
 */
function isEnabled(options) {
  var env = environmentOf(options);
  var raw = exactLookup(env)(defaults.BY_KEY.enabled.env);
  if (raw !== null) {
    var resolved = conv.resolvePlaceholders(raw, conv.environmentLookup(env), true);
    return conv.asciiLower(resolved) === 'true';
  }
  var value = options ? options.enabled : undefined;
  return value === true || (typeof value === 'string' && conv.asciiLower(value) === 'true');
}

function getPath(object, path) {
  var value = object;
  for (var i = 0; i < path.length; i++) {
    if (!isPlainObject(value)) {
      return undefined;
    }
    value = value[path[i]];
  }
  return value;
}

function optionName(path) {
  return 'options.' + path.join('.');
}

function isListType(type) {
  return type === 'intList' || type === 'stringList' || type === 'excludeList';
}

// Converts the text of a setting (environment value or string option).
function convertText(s, text) {
  switch (s.type) {
    case 'boolean':
      return conv.toBoolean(text);
    case 'enum':
      return conv.toEnum(text, s.values);
    case 'double':
      return conv.toDouble(text);
    case 'int':
      return conv.toInteger(text, 32);
    case 'long':
      return conv.toInteger(text, 64);
    case 'intList':
      return conv.splitList(text).map(function (element) {
        if (element.length === 0) {
          throw new RangeError('empty list element');
        }
        return conv.toInteger(element, 32);
      });
    case 'stringList':
      return conv.splitList(text);
    case 'excludeList':
      return conv.splitList(text).map(function (element) {
        if (element.length === 0) {
          throw new RangeError('empty list element');
        }
        return { path: element };
      });
    default:
      throw new Error('unknown setting type ' + s.type);
  }
}

// Converts a code option value; undefined means "not set".
function convertOption(s, value) {
  var name = optionName(s.path);
  if (typeof value === 'string' && s.type === 'enum') {
    var text = conv.settingText(value, function () {
      return null;
    }, false);
    return text === undefined ? undefined : convertText(s, text);
  }
  switch (s.type) {
    case 'boolean':
      if (typeof value !== 'boolean') {
        throw typeError(name + ' must be a boolean');
      }
      return value;
    case 'enum':
      throw typeError(name + ' must be a string');
    case 'double':
    case 'int':
    case 'long':
      if (typeof value !== 'number') {
        throw typeError(name + ' must be a number');
      }
      return value;
    case 'intList':
    case 'stringList':
      if (!Array.isArray(value)) {
        throw typeError(name + ' must be an array');
      }
      return value.slice();
    case 'excludeList':
      if (!Array.isArray(value)) {
        throw typeError(name + ' must be an array');
      }
      return value.map(function (entry, index) {
        if (typeof entry === 'string') {
          return { path: entry };
        }
        if (!isPlainObject(entry)) {
          throw typeError(name + '[' + index + '] must be a string or { path, reason }');
        }
        checkKeys(entry, defaults.EXCLUDE_KEYS, name + '[' + index + ']');
        return { path: entry.path, reason: entry.reason };
      });
    default:
      throw new Error('unknown setting type ' + s.type);
  }
}

function readSetting(s, options, envLookup, placeholderLookup) {
  var raw = envLookup(s.env);
  if (raw !== null) {
    var text;
    try {
      text = conv.settingText(raw, placeholderLookup, isListType(s.type));
      if (text !== undefined) {
        return convertText(s, text);
      }
    } catch (e) {
      throw configError('invalid ' + s.env + ' (' + s.property + '): ' + e.message);
    }
  }
  var value = getPath(options, s.path);
  if (value !== undefined && value !== null) {
    var converted;
    try {
      converted = convertOption(s, value);
    } catch (e) {
      if (e instanceof TypeError) {
        throw e;
      }
      throw configError('invalid ' + optionName(s.path) + ': ' + e.message);
    }
    if (converted !== undefined) {
      return converted;
    }
  }
  return s.defaultValue;
}

function checkKeys(object, allowed, label) {
  Object.keys(object).forEach(function (key) {
    if (allowed.indexOf(key) === -1) {
      throw typeError('unknown option ' + label + '.' + key);
    }
  });
}

/** CoverageReport.isBlank: empty or only Character.isWhitespace units (supplementary code points never are). */
function isBlankReason(reason) {
  if (reason === undefined || reason === null) {
    return true;
  }
  for (var i = 0; i < reason.length; i++) {
    if (!javaChars.isWhitespace(reason.charCodeAt(i))) {
      return false;
    }
  }
  return true;
}

// ---------------------------------------------------------------------------------------------------------------
// Paths

// Express matches routes with a case-insensitive regular expression (flag 'i', no 'u'): two units match when their
// canonical forms are equal. The canonical form of a unit is its upper case when that is one unit, except that a
// unit above U+007F never maps to ASCII. Non-ASCII results are kept per unit (at most 65,536 entries).
var canonicalUnits = new Map();
function canonicalUnit(c) {
  if (c < 0x80) {
    return c >= 0x61 && c <= 0x7A ? c - 0x20 : c;
  }
  var known = canonicalUnits.get(c);
  if (known !== undefined) {
    return known;
  }
  var upper = String.fromCharCode(c).toUpperCase();
  var result = upper.length === 1 && upper.charCodeAt(0) >= 0x80 ? upper.charCodeAt(0) : c;
  canonicalUnits.set(c, result);
  return result;
}

/**
 * The form in which a route path and a request path are compared: each unit in its case-insensitive canonical form,
 * and one trailing '/' dropped, except for the root. Matches Express's default routing semantics.
 */
function normalizePath(path) {
  var out = '';
  var changed = false;
  for (var i = 0; i < path.length; i++) {
    var c = path.charCodeAt(i);
    var k = canonicalUnit(c);
    if (k !== c && !changed) {
      changed = true;
      out = path.slice(0, i);
    }
    if (changed) {
      out += String.fromCharCode(k);
    }
  }
  var key = changed ? out : path;
  if (key.length > 1 && key.charCodeAt(key.length - 1) === 0x2F) {
    return key.slice(0, -1);
  }
  return key;
}

function prefixMatches(routeNorm, requestNorm) {
  if (routeNorm === '/') {
    return true;
  }
  if (requestNorm.length < routeNorm.length || requestNorm.slice(0, routeNorm.length) !== routeNorm) {
    return false;
  }
  return requestNorm.length === routeNorm.length || requestNorm.charCodeAt(routeNorm.length) === 0x2F;
}

// 2: same method, 1: a GET entry for a HEAD request, 0: an entry without a method; -1: no match.
function methodRank(entryMethod, requestMethod) {
  if (entryMethod === null) {
    return 0;
  }
  if (entryMethod === requestMethod) {
    return 2;
  }
  if (entryMethod === 'GET' && requestMethod === 'HEAD') {
    return 1;
  }
  return -1;
}

// ---------------------------------------------------------------------------------------------------------------
// Routes and exclusions

function compileEntry(raw, index, source, cfg) {
  var label = source === 'exclude' ? 'excludePaths[' + index + ']' : 'routes[' + index + ']';
  if (!isPlainObject(raw)) {
    throw typeError(label + ' must be an object');
  }
  if (source === 'route') {
    checkKeys(raw, defaults.ROUTE_KEYS, 'options.' + label);
  }
  if (typeof raw.path !== 'string' || raw.path.length === 0 || raw.path.charCodeAt(0) !== 0x2F) {
    throw configError(label + '.path must be a string starting with "/"');
  }
  if (raw.path.indexOf('?') !== -1) {
    throw configError(label + '.path must not contain "?"');
  }
  function optionalBoolean(key) {
    var v = raw[key];
    if (v !== undefined && typeof v !== 'boolean') {
      throw typeError(label + '.' + key + ' must be a boolean');
    }
    return v;
  }
  function optionalString(key) {
    var v = raw[key];
    if (v !== undefined && typeof v !== 'string') {
      throw typeError(label + '.' + key + ' must be a string');
    }
    return v;
  }
  var prefix = source === 'exclude' ? true : optionalBoolean('prefix') === true;
  var method = optionalString('method');
  if (method !== undefined && method.length === 0) {
    throw configError(label + '.method must not be empty');
  }
  method = method === undefined ? null : conv.asciiUpper(method);
  var reason = optionalString('reason');
  var name = optionalString('name');
  if (name === undefined) {
    name = (method === null ? '*' : method) + ' ' + raw.path;
  }
  var entry = {
    index: index,
    source: source,
    kind: prefix ? 'prefix' : 'exact',
    path: raw.path,
    norm: normalizePath(raw.path),
    method: method,
    name: name,
    enabled: source === 'exclude' ? false : optionalBoolean('enabled'),
    structured: optionalBoolean('structured'),
    mode: undefined,
    skipPaths: [],
    limits: {},
    reason: reason,
    prefix: prefix
  };
  if (raw.mode !== undefined) {
    if (typeof raw.mode !== 'string') {
      throw typeError(label + '.mode must be a string');
    }
    var mode;
    try {
      mode = conv.toEnum(raw.mode, defaults.MODES);
    } catch (e) {
      mode = null;
    }
    if (!mode) {
      throw configError('Invalid route ' + name + ': mode must be REPORT or ENFORCE');
    }
    entry.mode = mode;
  }
  if (raw.skipPaths !== undefined) {
    if (!Array.isArray(raw.skipPaths)) {
      throw typeError(label + '.skipPaths must be an array');
    }
    try {
      // Validates every pointer: an empty one fails startup naming the route, like emptySkipPathFailsStartupNamingTheHandler.
      new SkipPathMatcher(raw.skipPaths); // eslint-disable-line no-new
    } catch (e) {
      throw configError('Invalid route ' + name + ': ' + e.message);
    }
    entry.skipPaths = raw.skipPaths.slice();
  }
  if (raw.limits !== undefined) {
    if (!isPlainObject(raw.limits)) {
      throw typeError(label + '.limits must be an object');
    }
    try {
      InspectionLimits.fromObject(raw.limits, cfg.limitsObject);
    } catch (e) {
      throw configError('Invalid route ' + name + ': ' + e.message);
    }
    Object.keys(raw.limits).forEach(function (key) {
      if (raw.limits[key] !== undefined) {
        entry.limits[key] = raw.limits[key];
      }
    });
  }
  return entry;
}

function publicRoute(entry) {
  var out = { path: entry.path };
  if (entry.source === 'exclude') {
    if (entry.reason !== undefined) {
      out.reason = entry.reason;
    }
    return Object.freeze(out);
  }
  out.prefix = entry.prefix;
  if (entry.method !== null) {
    out.method = entry.method;
  }
  out.name = entry.name;
  if (entry.enabled !== undefined) {
    out.enabled = entry.enabled;
  }
  if (entry.structured !== undefined) {
    out.structured = entry.structured;
  }
  if (entry.mode !== undefined) {
    out.mode = entry.mode;
  }
  out.skipPaths = Object.freeze(entry.skipPaths.slice());
  out.limits = Object.freeze(Object.assign({}, entry.limits));
  if (entry.reason !== undefined) {
    out.reason = entry.reason;
  }
  return Object.freeze(out);
}

// ---------------------------------------------------------------------------------------------------------------
// Policy resolution

function PolicyResolver(cfg, entries) {
  this.cfg = cfg;
  this.prefixEntries = entries.filter(function (e) {
    return e.kind === 'prefix';
  });
  this.exactEntries = entries.filter(function (e) {
    return e.kind === 'exact';
  });
  this.memo = new Map();
  this.pairs = new Map();
  this.globalPolicy = null;
}

/** The merged policy for a class-level and a method-level entry (either may be null). */
PolicyResolver.prototype.merge = function (cls, mth) {
  var key = (cls ? cls.source + cls.index : '-') + '|' + (mth ? mth.source + mth.index : '-');
  var cached = this.pairs.get(key);
  if (cached) {
    return cached;
  }
  var cfg = this.cfg;
  var enabled = cfg.activation === 'ALL' || cls !== null || mth !== null;
  if (cls && cls.enabled !== undefined) {
    enabled = cls.enabled;
  }
  if (mth && mth.enabled !== undefined) {
    enabled = mth.enabled;
  }
  var structured = cfg.structuredDefault;
  var mode = cfg.mode;
  var limits = cfg.limitsObject;
  var skips = [];
  [cls, mth].forEach(function (d) {
    if (!d) {
      return;
    }
    if (d.structured !== undefined) {
      structured = d.structured;
    }
    if (d.mode !== undefined) {
      mode = d.mode;
    }
    d.skipPaths.forEach(function (p) {
      if (skips.indexOf(p) === -1) {
        skips.push(p);
      }
    });
    limits = InspectionLimits.fromObject(d.limits, limits);
  });
  var policy = Object.freeze({
    enabled: enabled,
    structured: structured,
    mode: mode,
    skipPaths: Object.freeze(skips),
    matcher: new SkipPathMatcher(skips),
    limits: limits,
    name: (mth && mth.name) || (cls && cls.name) || null
  });
  this.pairs.set(key, policy);
  return policy;
};

/** The class-level entry: the longest matching prefix entry; at equal length the better method match wins. */
PolicyResolver.prototype.classEntry = function (method, norm) {
  var best = null;
  var bestRank = -1;
  for (var i = 0; i < this.prefixEntries.length; i++) {
    var e = this.prefixEntries[i];
    var rank = methodRank(e.method, method);
    if (rank < 0 || !prefixMatches(e.norm, norm)) {
      continue;
    }
    if (best === null || e.norm.length > best.norm.length || (e.norm.length === best.norm.length && rank > bestRank)) {
      best = e;
      bestRank = rank;
    }
  }
  return best;
};

/** The method-level entry: the matching exact entry; a method-specific entry wins over one without a method. */
PolicyResolver.prototype.methodEntry = function (method, norm) {
  var best = null;
  var bestRank = -1;
  for (var i = 0; i < this.exactEntries.length; i++) {
    var e = this.exactEntries[i];
    var rank = methodRank(e.method, method);
    if (rank < 0 || e.norm !== norm) {
      continue;
    }
    if (best === null || rank > bestRank) {
      best = e;
      bestRank = rank;
    }
  }
  return best;
};

/** The effective policy for a request method and raw pathname; memoised in a bounded map. */
PolicyResolver.prototype.resolve = function (method, pathname) {
  var upper = conv.asciiUpper(typeof method === 'string' ? method : '');
  var norm = normalizePath(pathname);
  var key = upper + ' ' + norm;
  var policy = this.memo.get(key);
  if (policy !== undefined) {
    return policy;
  }
  policy = this.merge(this.classEntry(upper, norm), this.methodEntry(upper, norm));
  if (this.memo.size >= MEMO_LIMIT) {
    this.memo.clear();
  }
  this.memo.set(key, policy);
  return policy;
};

/** The policy a request that matches `entry` exactly would get (for the startup coverage report). */
PolicyResolver.prototype.forEntry = function (entry) {
  if (entry.kind === 'prefix') {
    return this.merge(entry, null);
  }
  var cls = null;
  for (var i = 0; i < this.prefixEntries.length; i++) {
    var p = this.prefixEntries[i];
    if ((p.method === null || p.method === entry.method) && prefixMatches(p.norm, entry.norm)
        && (cls === null || p.norm.length > cls.norm.length || (p.norm.length === cls.norm.length && p.method !== null))) {
      cls = p;
    }
  }
  return this.merge(cls, entry);
};

// ---------------------------------------------------------------------------------------------------------------
// The whole configuration

/**
 * Builds and validates the effective configuration of an enabled instance. Throws TypeError or RangeError (message
 * prefixed "request-validation: ") for any invalid setting.
 */
function resolveConfig(options) {
  var opts = options === undefined || options === null ? {} : options;
  if (!isPlainObject(opts)) {
    throw typeError('options must be an object');
  }
  checkKeys(opts, defaults.OPTION_KEYS, 'options');
  if (opts.log !== undefined && opts.log !== null) {
    if (!isPlainObject(opts.log)) {
      throw typeError('options.log must be an object');
    }
    checkKeys(opts.log, defaults.LOG_KEYS, 'options.log');
  }
  ['limits', 'rules'].forEach(function (group) {
    if (opts[group] !== undefined && opts[group] !== null) {
      if (!isPlainObject(opts[group])) {
        throw typeError('options.' + group + ' must be an object');
      }
      checkKeys(opts[group], group === 'limits' ? defaults.LIMIT_KEYS : defaults.RULE_KEYS, 'options.' + group);
    }
  });
  ['logger', 'logContext', 'respond'].forEach(function (key) {
    var v = opts[key];
    if (v !== undefined && v !== null && (key === 'logger' ? !(v && typeof v.warn === 'function') : typeof v !== 'function')) {
      throw typeError(key === 'logger' ? 'options.logger must have a warn(line) function' : 'options.' + key + ' must be a function');
    }
  });
  if (opts.env !== undefined && opts.env !== null && opts.env !== false && typeof opts.env !== 'object') {
    throw typeError('options.env must be an object or false');
  }

  var env = environmentOf(opts);
  var envLookup = exactLookup(env);
  var placeholderLookup = conv.environmentLookup(env);
  var v = {};
  defaults.SETTINGS.forEach(function (s) {
    if (s.key !== 'enabled') {
      v[s.key] = readSetting(s, opts, envLookup, placeholderLookup);
    }
  });

  if (v.structuredDefault === null) {
    throw configError('egov.request-validation.structured-default must be explicitly true or false '
        + '(EGOV_REQUEST_VALIDATION_STRUCTURED_DEFAULT or options.structuredDefault)');
  }
  if (defaults.MODES.indexOf(v.mode) === -1) {
    throw configError('egov.request-validation.mode must be REPORT or ENFORCE');
  }
  if (typeof v.reportSampleRate !== 'number' || !isFinite(v.reportSampleRate) || v.reportSampleRate < 0
      || v.reportSampleRate > 1) {
    throw configError('egov.request-validation.log.report-sample-rate must be between zero and one');
  }
  var limitsObject;
  try {
    limitsObject = new InspectionLimits(v.maxBodyBytes, v.maxDepth, v.maxStringLength, v.maxNameLength, v.maxTokens,
        v.maxNumberLength, v.maxScalarLength);
  } catch (e) {
    throw configError('invalid limits: ' + e.message);
  }
  var policy;
  try {
    policy = new ContentPolicy({
      markupStart: v.markupStart, urlScheme: v.urlScheme, eventHandler: v.eventHandler,
      disallowedControls: v.disallowedControls, deniedSchemes: v.deniedSchemes,
      deniedDataMediaTypes: v.deniedDataMediaTypes, decodeRounds: v.decodeRounds, normalizeNfkc: v.normalizeNfkc
    });
  } catch (e) {
    throw configError('invalid rules: ' + e.message);
  }
  if (policy.normalizeNfkc && !nfkc.selfTest()) {
    throw configError('NFKC normalization self-test failed on this runtime');
  }
  if (v.maxInspectionMillis !== null
      && (!Number.isInteger(v.maxInspectionMillis) || v.maxInspectionMillis < 1 || v.maxInspectionMillis > 2147483647)) {
    throw configError('maxInspectionMillis must be an integer of at least 1 (or unset)');
  }
  if (!Number.isInteger(v.slowInspectionWarnMillis) || v.slowInspectionWarnMillis < 0
      || v.slowInspectionWarnMillis > 2147483647) {
    throw configError('slowInspectionWarnMillis must be an integer of at least 0');
  }

  var cfg = {
    enabled: true,
    structuredDefault: v.structuredDefault,
    activation: v.activation,
    mode: v.mode,
    inspectContentTypeHeader: v.inspectContentTypeHeader,
    rejectDuplicateKeys: v.rejectDuplicateKeys,
    rejectDualRequestInfo: v.rejectDualRequestInfo,
    reportSampleRate: v.reportSampleRate,
    limitsObject: limitsObject,
    contentPolicy: policy,
    maxInspectionMillis: v.maxInspectionMillis,
    slowInspectionWarnMillis: v.slowInspectionWarnMillis
  };

  // Routes (code only) and exclusions
  var entries = [];
  if (opts.routes !== undefined && opts.routes !== null) {
    if (!Array.isArray(opts.routes)) {
      throw typeError('options.routes must be an array');
    }
    opts.routes.forEach(function (raw, index) {
      entries.push(compileEntry(raw, index, 'route', cfg));
    });
  }
  v.excludePaths.forEach(function (raw, index) {
    entries.push(compileEntry(raw, index, 'exclude', cfg));
  });
  var seen = {};
  entries.forEach(function (e) {
    var key = (e.method === null ? '*' : e.method) + ' ' + e.kind + ' ' + e.norm;
    if (seen[key]) {
      throw configError('two route entries with the same method, path and kind: ' + seen[key] + ' and ' + e.name);
    }
    seen[key] = e.name;
  });
  var resolver = new PolicyResolver(cfg, entries);

  var limitsPlain = Object.freeze(limitsObject.toJSON());
  var rulesPlain = policy.toJSON();
  Object.keys(rulesPlain).forEach(function (k) {
    if (Array.isArray(rulesPlain[k])) {
      rulesPlain[k] = Object.freeze(rulesPlain[k].slice());
    }
  });
  var effective = Object.freeze({
    enabled: true,
    structuredDefault: cfg.structuredDefault,
    activation: cfg.activation,
    mode: cfg.mode,
    inspectContentTypeHeader: cfg.inspectContentTypeHeader,
    rejectDuplicateKeys: cfg.rejectDuplicateKeys,
    rejectDualRequestInfo: cfg.rejectDualRequestInfo,
    reportSampleRate: cfg.reportSampleRate,
    limits: limitsPlain,
    rules: Object.freeze(rulesPlain),
    routes: Object.freeze(entries.filter(function (e) {
      return e.source === 'route';
    }).map(publicRoute)),
    excludePaths: Object.freeze(entries.filter(function (e) {
      return e.source === 'exclude';
    }).map(publicRoute)),
    maxInspectionMillis: cfg.maxInspectionMillis,
    slowInspectionWarnMillis: cfg.slowInspectionWarnMillis
  });

  return { internal: cfg, effective: effective, entries: entries, resolver: resolver };
}

module.exports = {
  isEnabled: isEnabled,
  resolveConfig: resolveConfig,
  environmentOf: environmentOf,
  normalizePath: normalizePath,
  prefixMatches: prefixMatches,
  isBlankReason: isBlankReason,
  PolicyResolver: PolicyResolver
};
