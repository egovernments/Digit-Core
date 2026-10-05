'use strict';

// The inspector's contract: Java's signature and argument checks, content findings through the consumer in document
// order, consumer errors propagating unchanged, no change to the input, and the Node-only options.

var assert = require('assert');
var h = require('./harness');
var test = h.test;

var JsonDocumentInspector = h.lib('JsonDocumentInspector');
var ContentDetector = h.lib('ContentDetector');
var ContentPolicy = h.lib('ContentPolicy');
var InspectionLimits = h.lib('InspectionLimits');
var SkipPathMatcher = h.lib('SkipPathMatcher');
var InspectionError = h.lib('InspectionError');
var Violation = h.lib('Violation');
var Tokenizer = h.lib('jackson/Tokenizer');

var detector = new ContentDetector(ContentPolicy.defaults());
var inspector = new JsonDocumentInspector(detector);
var NONE = new SkipPathMatcher([]);
var DEFAULTS = InspectionLimits.defaults();

function collect(body, limits, options) {
  var found = [];
  var failure = null;
  try {
    inspector.inspect(Buffer.isBuffer(body) ? body : Buffer.from(body, 'utf8'), limits || DEFAULTS, NONE, true, true,
        function (v) {
          found.push(String(v));
        }, options);
  } catch (e) {
    if (!(e instanceof InspectionError)) {
      throw e;
    }
    failure = String(e.violation);
  }
  return found.concat(failure === null ? [] : [failure]).join(';') || 'PASS';
}

test('constructor: a detector is required; the instance is frozen and exposes it', function () {
  assert.throws(function () {
    return new JsonDocumentInspector();
  }, TypeError);
  assert.throws(function () {
    return new JsonDocumentInspector(null);
  }, TypeError);
  assert.throws(function () {
    return new JsonDocumentInspector({});
  }, TypeError);
  assert.ok(Object.isFrozen(inspector));
  assert.strictEqual(inspector.detector, detector);
});

test('inspect: Java argument checks (TypeError for a missing or wrong argument)', function () {
  var ok = Buffer.from('{}');
  var fn = function () {};
  [
    [null, DEFAULTS, NONE, true, true, fn],
    ['{}', DEFAULTS, NONE, true, true, fn],
    [ok, null, NONE, true, true, fn],
    [ok, { maxBodyBytes: 10 }, NONE, true, true, fn],
    [ok, DEFAULTS, [], true, true, fn],
    [ok, DEFAULTS, NONE, 'yes', true, fn],
    [ok, DEFAULTS, NONE, true, 1, fn],
    [ok, DEFAULTS, NONE, true, true, null]
  ].forEach(function (args, i) {
    assert.throws(function () {
      inspector.inspect.apply(inspector, args);
    }, TypeError, 'argument set ' + i);
  });
});

test('content findings arrive in document order as Violations, before a later structural failure', function () {
  var seen = [];
  assert.throws(function () {
    inspector.inspect(Buffer.from('{"a":"<b>","<i>":["javascript:x",1],"a":2}'), DEFAULTS, NONE, true, true, function (v) {
      assert.ok(v instanceof Violation);
      assert.ok(Object.isFrozen(v));
      seen.push(String(v));
    });
  }, function (e) {
    return e instanceof InspectionError && String(e.violation) === 'REQUEST_JSON_DUPLICATE_KEY|duplicate-key|/a|1';
  });
  assert.deepStrictEqual(seen, [
    'REQUEST_CONTENT_NOT_ALLOWED|R1|/a|3',
    'REQUEST_CONTENT_NOT_ALLOWED|R1|/*|3',
    'REQUEST_CONTENT_NOT_ALLOWED|R2|/*/0|12'
  ]);
});

test('whatever the consumer throws propagates unchanged (including parser error types and InspectionErrors)', function () {
  var body = Buffer.from('["<b>","x\\q"]');
  var thrown = [
    'plain string',
    new Error('boom'),
    new Tokenizer.JsonParseError('not from the parser'),
    new Tokenizer.StreamConstraintsError('not from the parser'),
    new InspectionError(new Violation('REQUEST_CONTENT_NOT_ALLOWED', 'R1', '/0', 3))
  ];
  thrown.forEach(function (value) {
    var caught;
    try {
      inspector.inspect(body, DEFAULTS, NONE, true, true, function () {
        throw value;
      });
    } catch (e) {
      caught = e;
    }
    assert.strictEqual(caught, value);
  });
});

test('Uint8Array input works; the input bytes are never modified', function () {
  var text = '{"a":"<b>","b":[1,2,{"c":"\\u003Cx"}]}';
  var buf = Buffer.from(text, 'utf8');
  var before = h.sha256(buf);
  var u8 = new Uint8Array(buf.length + 4);
  u8.set(buf, 2);
  var view = u8.subarray(2, 2 + buf.length);
  assert.strictEqual(collect(Buffer.from(view)), collect(buf));
  var viaU8 = [];
  inspector.inspect(view, DEFAULTS, NONE, true, true, function (v) {
    viaU8.push(String(v));
  });
  assert.strictEqual(viaU8.join(';'), collect(buf));
  assert.strictEqual(h.sha256(buf), before);
  assert.strictEqual(u8[0], 0);
  assert.strictEqual(u8[u8.length - 1], 0);
});

test('one leading BOM is skipped; a second BOM or a BOM alone is malformed', function () {
  var bom = Buffer.from([0xEF, 0xBB, 0xBF]);
  assert.strictEqual(collect(Buffer.concat([bom, Buffer.from('{"a":"<b>"}')])), 'REQUEST_CONTENT_NOT_ALLOWED|R1|/a|3');
  assert.strictEqual(collect(Buffer.concat([bom, bom, Buffer.from('{}')])), 'REQUEST_JSON_MALFORMED|json|/|0');
  assert.strictEqual(collect(bom), 'REQUEST_JSON_MALFORMED|json|/|0');
});

test('inspect is re-entrant and the inspector keeps no per-call state', function () {
  var inner = [];
  var outer = collect('["<b>"]');
  inspector.inspect(Buffer.from('["<i>"]'), DEFAULTS, NONE, true, true, function () {
    inner.push(collect('{"x":"<u>"}'));
  });
  assert.deepStrictEqual(inner, ['REQUEST_CONTENT_NOT_ALLOWED|R1|/x|3']);
  assert.strictEqual(collect('["<b>"]'), outer);
});

test('Node-only deadline: checked every 256 tokens and after a name or string longer than 4,096 units', function () {
  var expired = { deadline: 0, now: function () {
    return 1;
  } };
  // after a long string: the next check names the string's location
  assert.strictEqual(collect('["' + 'x'.repeat(5000) + '",1]', DEFAULTS, expired),
      'REQUEST_LIMIT_EXCEEDED|inspection-budget|/0|0');
  // the 256-token check: tokens 1..255 are '[' and 254 numbers, so the location is the last number
  var many = '[' + new Array(300).join('1,') + '1]';
  assert.strictEqual(collect(many, DEFAULTS, expired), 'REQUEST_LIMIT_EXCEEDED|inspection-budget|/253|0');
  // not expired, or no deadline: Java behaviour
  var later = { deadline: 10, now: function () {
    return 1;
  } };
  assert.strictEqual(collect(many, DEFAULTS, later), 'PASS');
  assert.strictEqual(collect(many, DEFAULTS, { now: function () {
    throw new Error('the clock is not read without a deadline');
  } }), 'PASS');
});

test('Node-only textBufferPool: emulates the recycled parser buffer of a Java thread', function () {
  // Java: on a fresh thread this probe fails at the first 200-unit text segment (parser-limit); after the same thread
  // parsed a long escaped string, the first segment is large, the bad escape is reached first (json).
  var probe = Buffer.from('{"a":"\\n' + 'x'.repeat(300) + '\\q"}');
  var limit100 = InspectionLimits.fromObject({ maxStringLength: 100 });
  assert.strictEqual(collect(probe, limit100), 'REQUEST_LIMIT_EXCEEDED|parser-limit|/a|0');
  var pool = { size: 0 };
  assert.strictEqual(collect('{"w":"\\n' + 'y'.repeat(200000) + '"}', DEFAULTS, { textBufferPool: pool }), 'PASS');
  assert.ok(pool.size > 0);
  assert.strictEqual(collect(probe, limit100, { textBufferPool: pool }), 'REQUEST_JSON_MALFORMED|json|/a|0');
  // without a pool every call starts fresh
  assert.strictEqual(collect(probe, limit100), 'REQUEST_LIMIT_EXCEEDED|parser-limit|/a|0');
});

test('skip paths suppress content detection only; structural checks still apply inside', function () {
  var skip = new SkipPathMatcher(['/a', '/b/*/c']);
  var found = [];
  inspector.inspect(Buffer.from('{"a":{"<x>":"<y>"},"b":[{"c":"<z>","d":"<w>"}]}'), DEFAULTS, skip, true, true, function (v) {
    found.push(String(v));
  });
  assert.deepStrictEqual(found, ['REQUEST_CONTENT_NOT_ALLOWED|R1|/b/0/d|3']);
  assert.throws(function () {
    inspector.inspect(Buffer.from('{"a":{"k":1,"k":2}}'), DEFAULTS, skip, true, true, function () {});
  }, function (e) {
    return String(e.violation) === 'REQUEST_JSON_DUPLICATE_KEY|duplicate-key|/a/k|1';
  });
});
