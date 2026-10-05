'use strict';

// ValidationReporter: the per-request cap of nine findings plus one reserved line for incomplete inspection.
// ENFORCE: the first finding of any kind is logged (even when the cap is full) and becomes the rejection.
// REPORT: content findings are logged and inspection continues; a structural or limit finding is logged with one
// inspection-incomplete line per request, and the caller stops inspecting that part of the request.

var Violation = require('../core/Violation');
var RequestValidationError = require('./errorResponse').RequestValidationError;

var MAX_FINDINGS = 9;
var INCOMPLETE_RULE = 'inspection-incomplete';

function Reporter(audit) {
  this.audit = audit;
}

Reporter.prototype.record = function (ctx, mode, violation, kind) {
  var state = ctx.state;
  if (mode === 'ENFORCE' || state.count < MAX_FINDINGS) {
    state.count++;
    this.audit.violation(mode, violation, kind, ctx);
  }
};

/** A content finding: always recorded; returns the rejection in ENFORCE, else null. */
Reporter.prototype.content = function (ctx, mode, violation, kind) {
  this.record(ctx, mode, violation, kind);
  return mode === 'ENFORCE' ? new RequestValidationError(violation.code) : null;
};

/** A finding that ends the request: logged as ENFORCE; returns the rejection. */
Reporter.prototype.rejected = function (ctx, violation, kind) {
  this.record(ctx, 'ENFORCE', violation, kind);
  return new RequestValidationError(violation.code);
};

/** A syntax, encoding or limit finding: ENFORCE returns the rejection; REPORT records it and the incomplete line. */
Reporter.prototype.structural = function (ctx, mode, violation, kind) {
  if (mode === 'ENFORCE') {
    return this.rejected(ctx, violation, kind);
  }
  this.record(ctx, mode, violation, kind);
  this.incomplete(ctx, violation.code, kind);
  return null;
};

/** Records once per request that REPORT inspection did not cover the whole request. */
Reporter.prototype.incomplete = function (ctx, code, kind) {
  var state = ctx.state;
  if (state.incomplete) {
    return;
  }
  state.incomplete = true;
  this.audit.violation('REPORT', new Violation(code, INCOMPLETE_RULE, '/', 0), kind, ctx);
};

Reporter.MAX_FINDINGS = MAX_FINDINGS;
Reporter.INCOMPLETE_RULE = INCOMPLETE_RULE;

module.exports = Reporter;
