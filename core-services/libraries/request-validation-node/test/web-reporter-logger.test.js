'use strict';

// REPORT/ENFORCE recording (ValidationReporter) and the log line (ValidationAuditLogger): the nine-finding cap with
// one reserved incomplete line, ENFORCE lines exempt from the cap and from sampling, safeText, logContext.

var assert = require('assert');
var path = require('path');
var h = require('./harness');
var test = h.test;
var Violation = require(path.join(h.ROOT, 'lib', 'core', 'Violation'));
var Reporter = require(path.join(h.ROOT, 'lib', 'web', 'reporter'));
var logging = require(path.join(h.ROOT, 'lib', 'web', 'auditLogger'));
var mini = require('./support/miniapp');

function setup(rate, random, logContext) {
  var logger = mini.recordingLogger();
  var audit = new logging.AuditLogger(logger, rate === undefined ? 1 : rate, logContext, random);
  var reporter = new Reporter(audit);
  var state = { count: 0, incomplete: false };
  var ctx = { req: { headers: {} }, state: state, handler: 'POST /x', method: 'POST' };
  return { logger: logger, reporter: reporter, ctx: ctx, state: state };
}

function r1(location) {
  return new Violation('REQUEST_CONTENT_NOT_ALLOWED', 'R1', location || '/a', 3);
}

test('finding line: exact template and field order (ValidationAuditLogger)', function () {
  var s = setup();
  s.reporter.content(s.ctx, 'REPORT', new Violation('REQUEST_CONTENT_NOT_ALLOWED', 'R3', '/RequestInfo/name', 12), 'body');
  assert.deepStrictEqual(s.logger.lines, [{ level: 'warn', line: 'request_validation mode=REPORT code=REQUEST_CONTENT_NOT_ALLOWED rule=R3 location=/RequestInfo/name kind=body handler=POST /x method=POST length=12' }]);
});

test('REPORT: 12 findings give 10 lines, nine findings and one inspection-incomplete', function () {
  var s = setup();
  for (var i = 0; i < 12; i++) {
    assert.strictEqual(s.reporter.content(s.ctx, 'REPORT', r1('/' + i), 'body'), null);
  }
  assert.strictEqual(s.reporter.structural(s.ctx, 'REPORT', new Violation('REQUEST_JSON_MALFORMED', 'trailing-content', '/', 0), 'body'), null);
  var f = s.logger.findings();
  assert.strictEqual(f.length, 10);
  f.slice(0, 9).forEach(function (x, i) {
    assert.strictEqual(x.rule, 'R1');
    assert.strictEqual(x.location, '/' + i);
  });
  assert.strictEqual(f[9].rule, 'inspection-incomplete');
  assert.strictEqual(f[9].code, 'REQUEST_JSON_MALFORMED');
  assert.strictEqual(f[9].location, '/');
  assert.strictEqual(f[9].length, '0');
});

test('REPORT: a structural finding logs itself and the incomplete line once per request', function () {
  var s = setup();
  s.reporter.structural(s.ctx, 'REPORT', new Violation('REQUEST_LIMIT_EXCEEDED', 'scalar-limit', 'a', 21), 'query/form');
  s.reporter.content(s.ctx, 'REPORT', r1('/x'), 'body');
  s.reporter.structural(s.ctx, 'REPORT', new Violation('REQUEST_JSON_MALFORMED', 'json', '/', 0), 'body');
  assert.deepStrictEqual(s.logger.findings().map(function (f) {
    return f.rule + '@' + f.kind;
  }), ['scalar-limit@query/form', 'inspection-incomplete@query/form', 'R1@body', 'json@body']);
});

test('ENFORCE: the rejection is logged even after REPORT findings filled the cap (mixed modes)', function () {
  // Java: enforceRejectionIsLoggedEvenAfterReportFindingsFillTheCap (method REPORT, body parameter ENFORCE)
  var s = setup();
  for (var i = 0; i < 10; i++) {
    s.reporter.content(s.ctx, 'REPORT', new Violation('REQUEST_CONTENT_NOT_ALLOWED', 'R1', 'p' + i, 3), 'query/form');
  }
  var rejection = s.reporter.content(s.ctx, 'ENFORCE', new Violation('REQUEST_CONTENT_NOT_ALLOWED', 'R1', '/', 8), 'body');
  assert.ok(rejection && rejection.status === 400 && rejection.code === 'REQUEST_CONTENT_NOT_ALLOWED');
  var f = s.logger.findings();
  assert.strictEqual(f.length, 10);
  f.slice(0, 9).forEach(function (x) {
    assert.strictEqual(x.mode, 'REPORT');
  });
  assert.strictEqual(f[9].mode, 'ENFORCE');
  assert.strictEqual(f[9].kind, 'body');
});

test('ENFORCE: structural findings are rejections and never add an incomplete line', function () {
  var s = setup();
  var rejection = s.reporter.structural(s.ctx, 'ENFORCE', new Violation('REQUEST_LIMIT_EXCEEDED', 'max-body-bytes', '/', 0), 'body');
  assert.strictEqual(rejection.code, 'REQUEST_LIMIT_EXCEEDED');
  assert.deepStrictEqual(s.logger.findings().map(function (f) {
    return f.mode + ' ' + f.rule;
  }), ['ENFORCE max-body-bytes']);
});

test('sampling: rate 0 drops every REPORT line (including incomplete) and keeps ENFORCE lines; random >= rate drops', function () {
  var s = setup(0);
  s.reporter.content(s.ctx, 'REPORT', r1(), 'body');
  s.reporter.structural(s.ctx, 'REPORT', new Violation('REQUEST_JSON_MALFORMED', 'json', '/', 0), 'body');
  s.reporter.rejected(s.ctx, r1(), 'body');
  assert.deepStrictEqual(s.logger.findings().map(function (f) {
    return f.mode;
  }), ['ENFORCE']);
  assert.strictEqual(s.state.count, 3, 'the count increments before sampling');
  assert.strictEqual(s.state.incomplete, true, 'the incomplete slot is used even when its line is sampled out');
  var draws = [0.49, 0.5, 0.51];
  var t = setup(0.5, function () {
    return draws.shift();
  });
  t.reporter.content(t.ctx, 'REPORT', r1('/a'), 'body');
  t.reporter.content(t.ctx, 'REPORT', r1('/b'), 'body');
  t.reporter.content(t.ctx, 'REPORT', r1('/c'), 'body');
  assert.deepStrictEqual(t.logger.findings().map(function (f) {
    return f.location;
  }), ['/a']);
});

test('safeText: 256 units at most; outside 0x20-0x7E and < > become _', function () {
  assert.strictEqual(logging.safeText(null), '');
  assert.strictEqual(logging.safeText(undefined), '');
  assert.strictEqual(logging.safeText('a<b>c'), 'a_b_c');
  assert.strictEqual(logging.safeText('tab\tnl\ncr\rdel\u007fnbsp '), 'tab_nl_cr_del_nbsp_');
  assert.strictEqual(logging.safeText('😀'), '__');
  assert.strictEqual(logging.safeText('x'.repeat(300)).length, 256);
  assert.strictEqual(logging.safeText(' ~!'), ' ~!');
  assert.strictEqual(logging.safeText(42), '42');
});

test('handler, method and logContext pass through safeText; logContext runs once per request; failures are ignored', function () {
  var calls = 0;
  var s = setup(1, undefined, function () {
    calls++;
    return { cid: 'abc<1>', n: 7, ok: true, none: null, skip: undefined, 'bad key\n': 'v' };
  });
  s.ctx.handler = '/p<script>';
  s.ctx.method = 'GE\nT';
  s.reporter.content(s.ctx, 'REPORT', r1(), 'body');
  s.reporter.content(s.ctx, 'REPORT', r1(), 'body');
  assert.strictEqual(calls, 1);
  assert.strictEqual(s.logger.lines[0].line, 'request_validation mode=REPORT code=REQUEST_CONTENT_NOT_ALLOWED rule=R1 location=/a kind=body handler=/p_script_ method=GE_T length=3 cid=abc_1_ n=7 ok=true none= bad key_=v');
  var t = setup(1, undefined, function () {
    throw new Error('<secret input>');
  });
  t.reporter.content(t.ctx, 'REPORT', r1(), 'body');
  assert.ok(/length=3$/.test(t.logger.lines[0].line));
  var throwing = new logging.AuditLogger({ warn: function () {
    throw new Error('logger down');
  } }, 1);
  throwing.violation('ENFORCE', r1(), 'body', { req: {}, state: {}, handler: 'h', method: 'POST' });
});
