'use strict';

// Exemptions (ContentExemption in the Java library): which flagged values are offered, when, with what view of the
// body, and that without one nothing changes. Mirrors ContentExemptionTest and ExemptionResolutionTest.

var assert = require('assert');
var path = require('path');
var h = require('./harness');
var test = h.test;
var rv = require(path.join(h.ROOT, 'index.js'));
var mini = require('./support/miniapp');

var JsonDocumentInspector = h.lib('JsonDocumentInspector');
var ContentDetector = h.lib('ContentDetector');
var ContentPolicy = h.lib('ContentPolicy');
var InspectionLimits = h.lib('InspectionLimits');
var SkipPathMatcher = h.lib('SkipPathMatcher');
var InspectionError = h.lib('InspectionError');
var FlaggedValue = h.lib('FlaggedValue');

var inspector = new JsonDocumentInspector(new ContentDetector(ContentPolicy.defaults()));
var NONE = new SkipPathMatcher([]);
var GENEROUS = new InspectionLimits(100000, 64, 10000, 256, 10000, 1000, 4096);
var JSON_CT = { 'content-type': 'application/json' };
var HTML_CODES = ['EMAIL_BODY', 'EMAIL_NOTES'];

function LOCALIZATION(value) {
  return value.matches('/messages/*/message') && HTML_CODES.indexOf(value.sibling('code')) !== -1
      && value.value.toLowerCase().indexOf('<script') === -1;
}

function inspect(json, exemption, options) {
  var found = [];
  var opts = Object.assign({ exemption: exemption }, options || {});
  inspector.inspect(Buffer.from(json, 'utf8'), opts.limits || GENEROUS, opts.skips || NONE, opts.dup !== false, true,
      function (v) {
        found.push(v);
      }, { exemption: exemption });
  return found;
}

function locations(violations) {
  return violations.map(function (v) {
    return v.location;
  });
}

test('exemption: accepts only the values the rule allows', function () {
  var body = '{"RequestInfo":{},"messages":['
      + '{"code":"EMAIL_BODY","message":"<p>Hello <b>{name}</b></p>"},'
      + '{"code":"OTHER","message":"<p>not listed</p>"},'
      + '{"code":"EMAIL_NOTES","message":"<script>alert(1)</script>"},'
      + '{"code":"EMAIL_BODY","module":"<b>other field</b>"}]}';
  assert.deepStrictEqual(locations(inspect(body, LOCALIZATION)),
      ['/messages/1/message', '/messages/2/message', '/messages/3/module']);
  assert.deepStrictEqual(locations(inspect(body, null)),
      ['/messages/0/message', '/messages/1/message', '/messages/2/message', '/messages/3/module']);
});

test('exemption: sees a sibling that follows the value', function () {
  assert.deepStrictEqual(inspect('{"messages":[{"message":"<p>Hi</p>","locale":"en_IN","code":"EMAIL_BODY"}]}',
      LOCALIZATION), []);
});

test('exemption: never offered field names; keeps document order; true only means accept', function () {
  var calls = 0;
  assert.deepStrictEqual(locations(inspect('{"<b>":"x","a":"<b>"}', function () {
    calls++;
    return true;
  })), ['/*']);
  assert.strictEqual(calls, 1);
  var body = '{"a":"<i>","<b>":{"c":"javascript:x"},"d":["<u>"]}';
  assert.deepStrictEqual(locations(inspect(body, function () {
    return false;
  })), locations(inspect(body, null)));
  assert.deepStrictEqual(locations(inspect(body, null)), ['/a', '/*', '/*/c', '/d/0']);
  assert.deepStrictEqual(locations(inspect('{"a":"<b>"}', function () {
    return 'yes';
  })), ['/a']);
});

test('exemption: one that throws keeps the finding', function () {
  assert.deepStrictEqual(locations(inspect('{"a":"<b>"}', function () {
    throw new Error('broken rule');
  })), ['/a']);
});

test('exemption: a syntax failure reports earlier findings unchanged without consulting it', function () {
  var calls = 0;
  var found = [];
  assert.throws(function () {
    inspector.inspect(Buffer.from('{"a":"<b>","b":}'), GENEROUS, NONE, true, true, function (v) {
      found.push(v);
    }, { exemption: function () {
      calls++;
      return true;
    } });
  }, function (e) {
    return e instanceof InspectionError && e.violation.code === 'REQUEST_JSON_MALFORMED';
  });
  assert.deepStrictEqual(locations(found), ['/a']);
  assert.strictEqual(calls, 0);
});

test('exemption: a limit failure behaves as without one', function () {
  var tight = new InspectionLimits(100000, 64, 10000, 256, 5, 1000, 4096);
  var body = Buffer.from('{"a":"<b>","b":1,"c":2,"d":3}');
  function run(options) {
    var found = [];
    var failure = null;
    try {
      inspector.inspect(body, tight, NONE, true, true, function (v) {
        found.push(String(v));
      }, options);
    } catch (e) {
      failure = String(e.violation);
    }
    return found.concat([failure]);
  }
  assert.deepStrictEqual(run({ exemption: function () {
    return true;
  } }), run(undefined));
});

test('exemption: skipped paths are not offered', function () {
  var calls = 0;
  var found = locations(inspect('{"skip":"<b>","keep":"<b>"}', function () {
    calls++;
    return false;
  }, { skips: new SkipPathMatcher(['/skip']) }));
  assert.deepStrictEqual(found, ['/keep']);
  assert.strictEqual(calls, 1);
});

test('exemption: FlaggedValue exposes path, pointer, rule and the other strings of the body', function () {
  var seen = [];
  inspect('{"Mdms":{"schemaCode":"HCM.AppFieldType","isActive":true,"n":7,'
      + '"data":{"a/b":{"x~y":"List<String>"}},"list":["<u>"]}}', function (v) {
    seen.push(v);
    return false;
  });
  assert.strictEqual(seen.length, 2);
  var type = seen[0];
  assert.ok(type instanceof FlaggedValue);
  assert.deepStrictEqual(type.path.slice(), ['Mdms', 'data', 'a/b', 'x~y']);
  assert.strictEqual(type.pointer, '/Mdms/data/a~1b/x~0y');
  assert.strictEqual(type.value, 'List<String>');
  assert.strictEqual(type.rule, 'R1');
  assert.ok(type.matches('/Mdms/data/*/x~0y'));
  assert.ok(!type.matches('/Mdms/data'));
  assert.ok(!type.matches('/Mdms/data/*/x~0y/z'));
  assert.strictEqual(type.string('/Mdms/schemaCode'), 'HCM.AppFieldType');
  assert.strictEqual(type.string('/Mdms/data/a~1b/x~0y'), 'List<String>');
  assert.strictEqual(type.string('/Mdms/isActive'), null);
  assert.strictEqual(type.string('/Mdms/n'), null);
  assert.strictEqual(type.string('/Mdms/missing'), null);
  assert.strictEqual(type.sibling('schemaCode'), null);
  assert.throws(function () { type.string('Mdms'); }, RangeError);
  assert.throws(function () { type.string('/a~2'); }, RangeError);
  assert.strictEqual(String(type).indexOf('List<String>'), -1);
  assert.strictEqual(seen[1].pointer, '/Mdms/list/0');
  assert.strictEqual(seen[1].sibling('schemaCode'), null);
  assert.ok(Object.isFrozen(type) && Object.isFrozen(type.path));
});

test('exemption: a bare string body has the root pointer; with duplicates allowed the last occurrence wins', function () {
  var seen = [];
  inspect('"<b>"', function (v) {
    seen.push(v);
    return false;
  });
  assert.strictEqual(seen[0].pointer, '');
  assert.strictEqual(seen[0].string(''), '<b>');
  assert.strictEqual(seen[0].sibling('x'), null);
  var codes = [];
  var found = inspect('{"code":"EMAIL_BODY","message":"<p>x</p>","code":"OTHER"}', function (v) {
    codes.push(v.sibling('code'));
    return HTML_CODES.indexOf(v.sibling('code')) !== -1;
  }, { dup: false });
  assert.deepStrictEqual(codes, ['OTHER']);
  assert.deepStrictEqual(locations(found), ['/message']);
});

test('exemption: without one, every option shape gives the same findings; a non-function is a TypeError', function () {
  ['{"a":"<b>"}', '["ok",["javascript:x"]]', '"<b>"', '{"onload=":true}',
    '{"x":{"y":[{"z":"\\u0001"}]},"<i>":"<u>"}', '{"RequestInfo":{"a":"<svg onload=1>"}}'].forEach(function (body) {
    var plain = [];
    var nulled = [];
    inspector.inspect(Buffer.from(body), GENEROUS, NONE, true, true, function (v) {
      plain.push(String(v));
    });
    inspector.inspect(Buffer.from(body), GENEROUS, NONE, true, true, function (v) {
      nulled.push(String(v));
    }, { exemption: null });
    assert.deepStrictEqual(nulled, plain, body);
  });
  assert.throws(function () {
    inspector.inspect(Buffer.from('{}'), GENEROUS, NONE, true, true, function () {}, { exemption: true });
  }, TypeError);
});

function instance(routes, extra) {
  var logger = mini.recordingLogger();
  var r = rv.createRequestValidation(Object.assign({ env: false, enabled: true, structuredDefault: true,
    activation: 'ALL', mode: 'ENFORCE', logger: logger, routes: routes }, extra || {}));
  return { rv: r, logger: logger };
}

test('exemption: a route exemption applies to that route only, through the verify hook', function () {
  var t = instance([{ path: '/json', method: 'POST', exemption: LOCALIZATION, reason: 'HTML email templates' }]);
  var app = mini.serviceApp(t.rv);
  var allowed = '{"messages":[{"message":"<p>Hi</p>","code":"EMAIL_BODY"}]}';
  return mini.request(app, { method: 'POST', url: '/json', headers: JSON_CT, body: allowed }).then(function (r) {
    assert.strictEqual(r.status, 200);
    assert.strictEqual(r.handled, true);
    return mini.request(app, { method: 'POST', url: '/json', headers: JSON_CT,
      body: '{"messages":[{"message":"<p>Hi</p>","code":"OTHER"}]}' });
  }).then(function (r) {
    assert.strictEqual(r.status, 400);
    return mini.request(app, { method: 'POST', url: '/other', headers: JSON_CT, body: allowed });
  }).then(function (r) {
    assert.strictEqual(r.status, 400);
    assert.ok(t.logger.lines.some(function (l) {
      return l.line === 'request_validation_exclusion handler=POST /json reason=HTML email templates';
    }));
  });
});

test('exemption: the safety-net walk of a parsed body applies it too, siblings in any order', function () {
  var t = instance([{ path: '/x', exemption: LOCALIZATION, reason: 'HTML email templates' }]);
  var app = new mini.App();
  app.use(t.rv.beforeParsers);
  app.use(mini.bodyParser('json', {}));
  app.use(t.rv.afterParsers);
  app.use(mini.echo);
  return mini.request(app, { method: 'POST', url: '/x', headers: JSON_CT,
    body: '{"messages":[{"message":"<p>Hi</p>","code":"EMAIL_BODY"}]}' }).then(function (r) {
    assert.strictEqual(r.status, 200);
    return mini.request(app, { method: 'POST', url: '/x', headers: JSON_CT,
      body: '{"messages":[{"<b>":"x","message":"<p>Hi</p>","code":"EMAIL_BODY"}]}' });
  }).then(function (r) {
    assert.strictEqual(r.status, 400);
    return mini.request(app, { method: 'POST', url: '/x', headers: JSON_CT,
      body: '{"messages":[{"message":"<script>x</script>","code":"EMAIL_BODY"}]}' });
  }).then(function (r) {
    assert.strictEqual(r.status, 400);
  });
});

test('exemption: config must be a function; a missing reason warns; inspectJson accepts one', function () {
  assert.throws(function () {
    instance([{ path: '/json', exemption: 'EMAIL_BODY' }]);
  }, TypeError);
  var t = instance([{ path: '/json', exemption: LOCALIZATION }]);
  assert.ok(t.logger.lines.some(function (l) {
    return l.level === 'warn' && l.line === 'request_validation_exclusion_missing_reason handler=* /json';
  }));
  var body = Buffer.from('{"messages":[{"message":"<p>Hi</p>","code":"EMAIL_BODY"}]}');
  assert.deepStrictEqual(t.rv.inspectJson(body, { exemption: LOCALIZATION }), { findings: [], failure: null });
  assert.strictEqual(t.rv.inspectJson(body).findings.length, 1);
});
