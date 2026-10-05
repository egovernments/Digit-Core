'use strict';

// Port of Violation: an immutable finding. The rule id must be a safe token and the location is re-sanitized, so
// no request text can reach a log or response through a Violation.

var ViolationCode = require('./ViolationCode');
var SafeLocationFormatter = require('./SafeLocationFormatter');

class Violation {
  /**
   * @param {string} code one of the four ViolationCode values
   * @param {string} ruleId [A-Za-z0-9_.-]{1,64} (R1-R4 or a structural/limit rule id)
   * @param {string} location sanitized with SafeLocationFormatter.sanitizeLocation
   * @param {number} length a non-negative integer (UTF-16 length of the offending value, or 0)
   */
  constructor(code, ruleId, location, length) {
    if (!ViolationCode.isViolationCode(code)) {
      throw new TypeError('code');
    }
    if (typeof ruleId !== 'string') {
      throw new TypeError('ruleId');
    }
    if (!SafeLocationFormatter.isSafeSegment(ruleId)) {
      throw new RangeError('ruleId is not safe');
    }
    var safeLocation = SafeLocationFormatter.sanitizeLocation(location);
    if (typeof length !== 'number' || !Number.isSafeInteger(length)) {
      throw new TypeError('length must be an integer');
    }
    if (length < 0) {
      throw new RangeError('length must not be negative');
    }
    this.code = code;
    this.ruleId = ruleId;
    this.location = safeLocation;
    this.length = length;
    Object.freeze(this);
  }

  /** "code|ruleId|location|length". */
  toString() {
    return this.code + '|' + this.ruleId + '|' + this.location + '|' + this.length;
  }
}

module.exports = Violation;
