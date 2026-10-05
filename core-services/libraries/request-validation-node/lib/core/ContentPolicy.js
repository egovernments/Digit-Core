'use strict';

// Port of ContentPolicy: immutable configuration of the four content rules, validated at construction.

var chars = require('./jdk/javaChars');

var KEYS = Object.freeze([
  'markupStart', 'urlScheme', 'eventHandler', 'disallowedControls', 'deniedSchemes', 'deniedDataMediaTypes',
  'decodeRounds', 'normalizeNfkc'
]);

var DEFAULTS = Object.freeze({
  markupStart: true,
  urlScheme: true,
  eventHandler: true,
  disallowedControls: Object.freeze([0]),
  deniedSchemes: Object.freeze(['javascript', 'vbscript']),
  deniedDataMediaTypes: Object.freeze(['text/html', 'application/xhtml+xml', 'image/svg+xml']),
  decodeRounds: 2,
  normalizeNfkc: false
});

function booleanOption(value, fallback, name) {
  if (value === undefined) {
    return fallback;
  }
  if (typeof value !== 'boolean') {
    throw new TypeError(name + ' must be a boolean');
  }
  return value;
}

function toList(values, label) {
  if (values === null || values === undefined || typeof values === 'string'
      || typeof values[Symbol.iterator] !== 'function') {
    throw new TypeError(label + ' must be an array or a set');
  }
  return Array.from(values);
}

// immutableControls: every value 0-31 except 9, 10 and 13; duplicates collapse, first occurrence order kept.
function immutableControls(values) {
  var list = toList(values, 'disallowedControls');
  var copy = [];
  for (var i = 0; i < list.length; i++) {
    var value = list[i];
    if (typeof value !== 'number' || !Number.isInteger(value) || value < 0 || value > 31
        || value === 9 || value === 10 || value === 13) {
      throw new RangeError('disallowedControls must contain only C0 controls other than TAB, LF and CR');
    }
    if (copy.indexOf(value) === -1) {
      copy.push(value);
    }
  }
  return Object.freeze(copy);
}

// immutableLowercase: trim (String.trim), reject blank, lower-case with Locale.ROOT; duplicates collapse.
function immutableLowercase(values, label) {
  var list = toList(values, label);
  var copy = [];
  for (var i = 0; i < list.length; i++) {
    var value = list[i];
    if (value !== null && value !== undefined && typeof value !== 'string') {
      throw new TypeError(label + ' must contain only strings');
    }
    if (value === null || value === undefined || chars.javaTrim(value).length === 0) {
      throw new RangeError(label + ' contains a blank value');
    }
    var lowered = chars.toLowerCaseRoot(chars.javaTrim(value));
    if (copy.indexOf(lowered) === -1) {
      copy.push(lowered);
    }
  }
  return Object.freeze(copy);
}

var defaultPolicy = null;

class ContentPolicy {
  /**
   * @param {object} [options] any of markupStart, urlScheme, eventHandler (booleans), disallowedControls (integers),
   *   deniedSchemes, deniedDataMediaTypes (strings), decodeRounds (0-3), normalizeNfkc (boolean). An undefined
   *   field takes the Java builder default; unknown keys are rejected. Lists are copied.
   */
  constructor(options) {
    var o = options === undefined ? {} : options;
    if (o === null || typeof o !== 'object' || Array.isArray(o)) {
      throw new TypeError('ContentPolicy options must be an object');
    }
    Object.keys(o).forEach(function (key) {
      if (KEYS.indexOf(key) === -1) {
        throw new TypeError('unknown ContentPolicy option: ' + key);
      }
    });
    var markupStart = booleanOption(o.markupStart, DEFAULTS.markupStart, 'markupStart');
    var urlScheme = booleanOption(o.urlScheme, DEFAULTS.urlScheme, 'urlScheme');
    var eventHandler = booleanOption(o.eventHandler, DEFAULTS.eventHandler, 'eventHandler');
    var normalizeNfkc = booleanOption(o.normalizeNfkc, DEFAULTS.normalizeNfkc, 'normalizeNfkc');
    // Java validation order: controls, schemes, media types, decode rounds.
    var disallowedControls = immutableControls(
        o.disallowedControls === undefined ? DEFAULTS.disallowedControls : o.disallowedControls);
    var deniedSchemes = immutableLowercase(
        o.deniedSchemes === undefined ? DEFAULTS.deniedSchemes : o.deniedSchemes, 'deniedSchemes');
    var deniedDataMediaTypes = immutableLowercase(
        o.deniedDataMediaTypes === undefined ? DEFAULTS.deniedDataMediaTypes : o.deniedDataMediaTypes,
        'deniedDataMediaTypes');
    var decodeRounds = o.decodeRounds === undefined ? DEFAULTS.decodeRounds : o.decodeRounds;
    if (typeof decodeRounds !== 'number' || !Number.isInteger(decodeRounds)) {
      throw new TypeError('decodeRounds must be an integer');
    }
    if (decodeRounds < 0 || decodeRounds > 3) {
      throw new RangeError('decodeRounds must be between 0 and 3');
    }
    this.markupStart = markupStart;
    this.urlScheme = urlScheme;
    this.eventHandler = eventHandler;
    this.disallowedControls = disallowedControls;
    this.deniedSchemes = deniedSchemes;
    this.deniedDataMediaTypes = deniedDataMediaTypes;
    this.decodeRounds = decodeRounds;
    this.normalizeNfkc = normalizeNfkc;
    Object.freeze(this);
  }

  /** The Java builder defaults. */
  static defaults() {
    if (defaultPolicy === null) {
      defaultPolicy = new ContentPolicy();
    }
    return defaultPolicy;
  }

  /** A plain object with the eight fields (lists copied). */
  toJSON() {
    return {
      markupStart: this.markupStart,
      urlScheme: this.urlScheme,
      eventHandler: this.eventHandler,
      disallowedControls: this.disallowedControls.slice(),
      deniedSchemes: this.deniedSchemes.slice(),
      deniedDataMediaTypes: this.deniedDataMediaTypes.slice(),
      decodeRounds: this.decodeRounds,
      normalizeNfkc: this.normalizeNfkc
    };
  }
}

ContentPolicy.KEYS = KEYS;
ContentPolicy.DEFAULTS = DEFAULTS;

module.exports = ContentPolicy;
