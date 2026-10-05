'use strict';

// The four violation codes and their fixed client messages (ViolationCode.java). Every finding, whether content,
// structural or a limit, carries one of these codes; R1-R4 and the other rule ids are separate strings.

var VALUES = Object.freeze([
  'REQUEST_CONTENT_NOT_ALLOWED',
  'REQUEST_JSON_MALFORMED',
  'REQUEST_LIMIT_EXCEEDED',
  'REQUEST_JSON_DUPLICATE_KEY'
]);

var VIOLATION_MESSAGES = Object.freeze({
  REQUEST_CONTENT_NOT_ALLOWED: 'Request contains content that is not allowed',
  REQUEST_JSON_MALFORMED: 'Request body is not valid JSON',
  REQUEST_LIMIT_EXCEEDED: 'Request exceeds an allowed size limit',
  REQUEST_JSON_DUPLICATE_KEY: 'Request body contains a duplicate property'
});

function isViolationCode(value) {
  return typeof value === 'string' && VALUES.indexOf(value) !== -1;
}

function getMessage(code) {
  if (!isViolationCode(code)) {
    throw new TypeError('code must be a ViolationCode');
  }
  return VIOLATION_MESSAGES[code];
}

// Enumerable keys are exactly the four codes (each mapped to its own name); helpers are non-enumerable.
var ViolationCode = {};
VALUES.forEach(function (code) {
  ViolationCode[code] = code;
});
Object.defineProperties(ViolationCode, {
  VALUES: { value: VALUES },
  VIOLATION_MESSAGES: { value: VIOLATION_MESSAGES },
  isViolationCode: { value: isViolationCode },
  getMessage: { value: getMessage }
});

module.exports = Object.freeze(ViolationCode);
