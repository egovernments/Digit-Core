'use strict';

// Port of InspectionLimits: seven validated resource limits. Each must be a positive integer no larger than its
// ceiling; boundaries are inclusive (a value equal to its limit passes, limit + 1 fails).

var FIELDS = Object.freeze([
  'maxBodyBytes', 'maxDepth', 'maxStringLength', 'maxNameLength', 'maxTokens', 'maxNumberLength', 'maxScalarLength'
]);

var MAX_BODY_BYTES_CEILING = 2147483646; // Integer.MAX_VALUE - 1
var MAX_DEPTH_CEILING = 256;
var MAX_TEXT_LENGTH_CEILING = 2147483646; // Integer.MAX_VALUE - 1
var MAX_TOKENS_CEILING = 2147483647;      // Integer.MAX_VALUE

var CEILINGS = Object.freeze({
  maxBodyBytes: MAX_BODY_BYTES_CEILING,
  maxDepth: MAX_DEPTH_CEILING,
  maxStringLength: MAX_TEXT_LENGTH_CEILING,
  maxNameLength: MAX_TEXT_LENGTH_CEILING,
  maxTokens: MAX_TOKENS_CEILING,
  maxNumberLength: MAX_TEXT_LENGTH_CEILING,
  maxScalarLength: MAX_TEXT_LENGTH_CEILING
});

var DEFAULTS = Object.freeze({
  maxBodyBytes: 10 * 1024 * 1024,
  maxDepth: 64,
  maxStringLength: 1000000,
  maxNameLength: 256,
  maxTokens: 2000000,
  maxNumberLength: 1000,
  maxScalarLength: 64 * 1024
});

function positiveAtMost(value, ceiling, name) {
  if (typeof value !== 'number' || !Number.isInteger(value)) {
    throw new TypeError(name + ' must be an integer');
  }
  if (value <= 0 || value > ceiling) {
    throw new RangeError(name + ' must be between 1 and ' + ceiling);
  }
  return value;
}

var defaultLimits = null;

class InspectionLimits {
  /** Arguments in the Java constructor's order. */
  constructor(maxBodyBytes, maxDepth, maxStringLength, maxNameLength, maxTokens, maxNumberLength, maxScalarLength) {
    this.maxBodyBytes = positiveAtMost(maxBodyBytes, MAX_BODY_BYTES_CEILING, 'maxBodyBytes');
    this.maxDepth = positiveAtMost(maxDepth, MAX_DEPTH_CEILING, 'maxDepth');
    this.maxStringLength = positiveAtMost(maxStringLength, MAX_TEXT_LENGTH_CEILING, 'maxStringLength');
    this.maxNameLength = positiveAtMost(maxNameLength, MAX_TEXT_LENGTH_CEILING, 'maxNameLength');
    this.maxTokens = positiveAtMost(maxTokens, MAX_TOKENS_CEILING, 'maxTokens');
    this.maxNumberLength = positiveAtMost(maxNumberLength, MAX_TEXT_LENGTH_CEILING, 'maxNumberLength');
    this.maxScalarLength = positiveAtMost(maxScalarLength, MAX_TEXT_LENGTH_CEILING, 'maxScalarLength');
    Object.freeze(this);
  }

  /** The Java defaults: 10 MiB body, depth 64, string 1,000,000, name 256, 2,000,000 tokens, number 1000, scalar 64 KiB. */
  static defaults() {
    if (defaultLimits === null) {
      defaultLimits = InspectionLimits.fromObject(DEFAULTS, null);
    }
    return defaultLimits;
  }

  /**
   * Builds limits from an object of the seven fields. A field that is undefined (or absent) takes its value from
   * `base` (default: the Java defaults); with base null every field is required. Unknown keys are rejected.
   */
  static fromObject(values, base) {
    if (values === null || typeof values !== 'object') {
      throw new TypeError('limits must be an object');
    }
    Object.keys(values).forEach(function (key) {
      if (FIELDS.indexOf(key) === -1) {
        throw new TypeError('unknown limit: ' + key);
      }
    });
    var fallback = base === undefined ? InspectionLimits.defaults() : base;
    if (fallback !== null && !(fallback instanceof InspectionLimits)) {
      throw new TypeError('base must be InspectionLimits or null');
    }
    var v = FIELDS.map(function (field) {
      var value = values[field];
      if (value === undefined) {
        if (fallback === null) {
          throw new TypeError(field + ' is required');
        }
        return fallback[field];
      }
      return value;
    });
    return new InspectionLimits(v[0], v[1], v[2], v[3], v[4], v[5], v[6]);
  }

  /** A plain object with the seven fields. */
  toJSON() {
    var out = {};
    for (var i = 0; i < FIELDS.length; i++) {
      out[FIELDS[i]] = this[FIELDS[i]];
    }
    return out;
  }
}

InspectionLimits.FIELDS = FIELDS;
InspectionLimits.CEILINGS = CEILINGS;
InspectionLimits.DEFAULTS = DEFAULTS;
InspectionLimits.MAX_BODY_BYTES_CEILING = MAX_BODY_BYTES_CEILING;
InspectionLimits.MAX_DEPTH_CEILING = MAX_DEPTH_CEILING;
InspectionLimits.MAX_TEXT_LENGTH_CEILING = MAX_TEXT_LENGTH_CEILING;
InspectionLimits.MAX_TOKENS_CEILING = MAX_TOKENS_CEILING;

module.exports = InspectionLimits;
