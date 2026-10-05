'use strict';

// Port of InspectionException: carries exactly one Violation. The message is the code's fixed message and the
// error never wraps a cause, so no parser message or request text travels with it.

var ViolationCode = require('./ViolationCode');
var Violation = require('./Violation');

class InspectionError extends Error {
  constructor(violation) {
    if (!(violation instanceof Violation)) {
      throw new TypeError('violation');
    }
    super(ViolationCode.getMessage(violation.code));
    Object.defineProperty(this, 'violation', { value: violation, enumerable: true });
  }
}

Object.defineProperty(InspectionError.prototype, 'name', {
  value: 'InspectionError',
  writable: true,
  configurable: true
});

module.exports = InspectionError;
