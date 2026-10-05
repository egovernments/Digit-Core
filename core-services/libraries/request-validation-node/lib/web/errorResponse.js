'use strict';

// ENFORCE responses: the bytes the Java stack (tracer) sends for a rejected request, status 400,
// Content-Type application/json, Connection: close, and never any request text, rule id or location.

var ViolationCode = require('../core/ViolationCode');

var BRAND = Symbol.for('@egovernments/request-validation.error');
var INTERNAL_CODE = 'INTERNAL_SERVER_ERROR';
var INTERNAL_MESSAGE = 'Internal server error';

function bodyFor(code, message) {
  return '{"ResponseInfo":null,"Errors":[{"code":"' + code + '","message":"' + message
      + '","description":null,"params":null}]}';
}

var BODIES = {};
ViolationCode.VALUES.forEach(function (code) {
  BODIES[code] = Buffer.from(bodyFor(code, ViolationCode.VIOLATION_MESSAGES[code]), 'utf8');
});
BODIES[INTERNAL_CODE] = Buffer.from(bodyFor(INTERNAL_CODE, INTERNAL_MESSAGE), 'utf8');

function messageFor(code) {
  return code === INTERNAL_CODE ? INTERNAL_MESSAGE : ViolationCode.getMessage(code);
}

/**
 * The error thrown in ENFORCE (and, with status 500, on an internal failure in ENFORCE). Carries only the fixed
 * code and message. status, statusCode and expose follow the http-errors convention, so body parsers and error
 * handlers that understand it keep the 400.
 */
class RequestValidationError extends Error {
  constructor(code) {
    var c = code === undefined ? 'REQUEST_CONTENT_NOT_ALLOWED' : code;
    if (c !== INTERNAL_CODE && !ViolationCode.isViolationCode(c)) {
      throw new TypeError('code must be a ViolationCode or INTERNAL_SERVER_ERROR');
    }
    super(messageFor(c));
    var status = c === INTERNAL_CODE ? 500 : 400;
    this.status = status;
    this.statusCode = status;
    this.expose = status < 500;
    this.code = c;
    Object.defineProperty(this, BRAND, { value: true });
  }
}

Object.defineProperty(RequestValidationError.prototype, 'name', {
  value: 'RequestValidationError',
  writable: true,
  configurable: true
});

/** True for errors created by this package (any copy of it): identified by brand, not by class or type. */
function isRequestValidationError(err) {
  return err !== null && (typeof err === 'object' || typeof err === 'function') && err[BRAND] === true;
}

/** The exact response body for a code, as a Buffer. */
function body(code) {
  return BODIES[code] || BODIES.REQUEST_CONTENT_NOT_ALLOWED;
}

/** Writes the default ENFORCE response. Returns false when the response had already started. */
function write(res, status, code) {
  if (res.headersSent) {
    return false;
  }
  res.statusCode = status;
  res.setHeader('Content-Type', 'application/json');
  res.setHeader('Connection', 'close');
  res.end(body(code));
  return true;
}

module.exports = {
  RequestValidationError: RequestValidationError,
  isRequestValidationError: isRequestValidationError,
  INTERNAL_CODE: INTERNAL_CODE,
  messageFor: messageFor,
  body: body,
  write: write,
  BRAND: BRAND
};
