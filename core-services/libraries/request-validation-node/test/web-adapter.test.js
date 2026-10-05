'use strict';

// The adapter through an Express-like stack (test/support/miniapp.js): mount points, scalars before the body, the
// grouped parameter map, JSON pre-checks, verify hooks, the error arm, path parameters, the safety net, internal
// errors, the time budget, and no mutation of the request.

var assert = require('assert');
var crypto = require('crypto');
var path = require('path');
var h = require('./harness');
var test = h.test;
var rv = require(path.join(h.ROOT, 'index.js'));
var mini = require('./support/miniapp');

var JSON_CT = { 'content-type': 'application/json' };
var FORM_CT = { 'content-type': 'application/x-www-form-urlencoded' };

function instance(extra) {
  var logger = mini.recordingLogger();
  var options = Object.assign({ env: false, enabled: true, structuredDefault: true, activation: 'ALL', mode: 'ENFORCE',
    logger: logger }, extra || {});
  var appOptions = options.appOptions;
  delete options.appOptions;
  var r = rv.createRequestValidation(options);
  logger.clear();
  return { rv: r, logger: logger, app: mini.serviceApp(r, appOptions) };
}

function send(t, options) {
  return mini.request(t.app, options);
}

function findingsOf(t) {
  return t.logger.findings().map(function (f) {
    return f.mode + ' ' + f.code + ' ' + f.rule + ' ' + f.location + ' ' + f.kind + ' ' + f.length;
  });
}

function blocked(r, code) {
  assert.strictEqual(r.status, 400);
  assert.strictEqual(r.handled, false);
  assert.strictEqual(r.headers['content-type'], 'application/json');
  assert.strictEqual(r.headers.connection, 'close');
  assert.strictEqual(JSON.parse(r.body).Errors[0].code, code);
}

test('ENFORCE JSON body: 400 with the fixed body, never the input; REPORT: handler sees the same bytes, one log line', function () {
  var e = instance();
  return send(e, { method: 'POST', url: '/json', headers: JSON_CT, body: '{"RequestInfo":{"name":"<script>"}}' }).then(function (r) {
    blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
    assert.strictEqual(r.body.indexOf('<script>'), -1);
    assert.deepStrictEqual(findingsOf(e), ['ENFORCE REQUEST_CONTENT_NOT_ALLOWED R1 /RequestInfo/name body 8']);
    var p = instance({ mode: 'REPORT', appOptions: { rawJson: true } });
    var body = ' { "note" : "O\'Brien & स्वास्थ्य < 5", "x": "<b>" }\n';
    return send(p, { method: 'POST', url: '/json', headers: { 'content-type': 'application/problem+json;charset=UTF-8' }, body: body })
        .then(function (r2) {
          assert.strictEqual(r2.status, 200);
          assert.strictEqual(Buffer.from(JSON.parse(r2.body).body, 'base64').toString('utf8'), body);
          assert.deepStrictEqual(findingsOf(p), ['REPORT REQUEST_CONTENT_NOT_ALLOWED R1 /x body 3']);
        });
  });
});

test('scalars run before the body: Content-Type, then query, then the JSON body', function () {
  var e = instance({ mode: 'REPORT' });
  return send(e, { method: 'POST', url: '/json?q=%3Cb%3E&ok=1', headers: { 'content-type': 'application/json; x="<s"' },
    body: '{"a":"<i>"}' }).then(function (r) {
    assert.strictEqual(r.status, 200);
    assert.deepStrictEqual(findingsOf(e), [
      'REPORT REQUEST_CONTENT_NOT_ALLOWED R1 /Content-Type header 24',
      'REPORT REQUEST_CONTENT_NOT_ALLOWED R1 q query/form 3',
      'REPORT REQUEST_CONTENT_NOT_ALLOWED R1 /a body 3']);
    var off = instance({ mode: 'REPORT', inspectContentTypeHeader: false });
    return send(off, { url: '/q', headers: { 'content-type': 'text/plain; x=<b>' } }).then(function () {
      assert.deepStrictEqual(findingsOf(off), []);
    });
  });
});

test('form bodies: query and form parameters grouped by name in Tomcat order decide the first ENFORCE code', function () {
  var big = 'a=' + 'y'.repeat(70000);
  var e = instance();
  return send(e, { method: 'POST', url: '/form?a=1&b=%3Cx', headers: FORM_CT, body: big }).then(function (r) {
    blocked(r, 'REQUEST_LIMIT_EXCEEDED');
    assert.deepStrictEqual(findingsOf(e), ['ENFORCE REQUEST_LIMIT_EXCEEDED scalar-limit a query/form 70000']);
    var f = instance();
    return send(f, { method: 'POST', url: '/form?b=%3Cx&a=1', headers: FORM_CT, body: big });
  }).then(function (r) {
    blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
    var g = instance({ mode: 'REPORT' });
    return send(g, { method: 'POST', url: '/form?x=1', headers: FORM_CT, body: 'n=%3Cb%3E&x=2&m=x+onclick%3Dalert(1)' }).then(function (r2) {
      assert.strictEqual(r2.status, 200);
      assert.deepStrictEqual(findingsOf(g), ['REPORT REQUEST_CONTENT_NOT_ALLOWED R1 n query/form 3',
        'REPORT REQUEST_CONTENT_NOT_ALLOWED R3 m query/form 18']);
    });
  });
});

test('malformed query pairs: Tomcat drops them, the views the application gets (qs, querystring) are inspected after the map (D3)', function () {
  var e = instance({ mode: 'REPORT' });
  return send(e, { url: '/q?z=%3Ci%3E&q=%3Cb%zz&w=1' }).then(function () {
    assert.deepStrictEqual(findingsOf(e), ['REPORT REQUEST_CONTENT_NOT_ALLOWED R1 z query/form 3',
      'REPORT REQUEST_CONTENT_NOT_ALLOWED R1 q query/form 7', 'REPORT REQUEST_CONTENT_NOT_ALLOWED R1 q query/form 5'],
    'qs keeps "%3Cb%zz", querystring decodes it partially to "<b%zz"');
  });
});

test('empty parameter names: inspected exactly where the application receives the value', function () {
  // The mini-app has no req.query, so the query parser is unknown: the value is inspected.
  var e = instance();
  return send(e, { url: '/q?=%3Cb' }).then(function (r) {
    blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
    assert.deepStrictEqual(findingsOf(e), ['ENFORCE REQUEST_CONTENT_NOT_ALLOWED R1 * query/form 2']);
    // An Express 4 style parsed query without the empty name (qs drops it): not inspected, as in Java.
    var q = instance({ appOptions: { before: [function (req, res, next) {
      req.query = {};
      next();
    }] } });
    return send(q, { url: '/q?=%3Cb' }).then(function (r2) {
      assert.strictEqual(r2.status, 200);
      assert.deepStrictEqual(findingsOf(q), []);
    });
  }).then(function () {
    // An Express 5 style parsed query that keeps it (Node's querystring): inspected.
    var k = instance({ appOptions: { before: [function (req, res, next) {
      req.query = { '': '<b' };
      next();
    }] } });
    return send(k, { url: '/q?=%3Cb' }).then(function (r) {
      blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
    });
  }).then(function () {
    // The mini-app's form parser is Node's querystring, which keeps the empty name: the parsed body is inspected.
    var f = instance();
    return send(f, { method: 'POST', url: '/form', headers: FORM_CT, body: 'a=1&=%3Cscript%3E' }).then(function (r) {
      blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
      assert.deepStrictEqual(findingsOf(f), ['ENFORCE REQUEST_CONTENT_NOT_ALLOWED R1 * query/form 8']);
    });
  });
});

test('a urlencoded parser with a wider type still has its body inspected (parser-bound hook)', function () {
  var t = instance();
  var app = new mini.App();
  app.use(t.rv.beforeParsers);
  app.use(mini.bodyParser('urlencoded', { verify: t.rv.formVerify, type: function () {
    return true;
  } }));
  app.use(t.rv.afterParsers);
  app.use(mini.echo);
  return mini.request(app, { method: 'POST', url: '/x?q=ok', headers: { 'content-type': 'text/plain' }, body: 'a=%3Cb%3E' })
      .then(function (r) {
        blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
        assert.deepStrictEqual(findingsOf(t), ['ENFORCE REQUEST_CONTENT_NOT_ALLOWED R1 a query/form 3']);
      });
});

test('JSON pre-checks: charset other than UTF-8 and declared length over the limit, before the body is read', function () {
  var e = instance();
  return send(e, { method: 'POST', url: '/json', headers: { 'content-type': 'application/json;charset=ISO-8859-1' }, body: '{}' })
      .then(function (r) {
        blocked(r, 'REQUEST_JSON_MALFORMED');
        assert.deepStrictEqual(findingsOf(e), ['ENFORCE REQUEST_JSON_MALFORMED charset / body 0']);
        return Promise.all(['utf-8', 'UTF8', '"utf-8"', 'unicode-1-1-utf-8'].map(function (cs) {
          var ok = instance();
          return send(ok, { method: 'POST', url: '/json', headers: { 'content-type': 'application/json; charset=' + cs }, body: '{}' })
              .then(function (r2) {
                assert.ok(r2.status === 200 || r2.status === 415, cs + ' ' + r2.status);
                assert.deepStrictEqual(findingsOf(ok), [], cs);
              });
        }));
      }).then(function () {
        var p = instance({ mode: 'REPORT' });
        return send(p, { method: 'POST', url: '/json', headers: { 'content-type': 'application/json;charset=ISO-8859-1' },
          body: Buffer.from('{"a":"café"}', 'latin1') }).then(function (r) {
          assert.strictEqual(r.status, 415, 'REPORT leaves the request to the host parser');
          assert.deepStrictEqual(findingsOf(p), ['REPORT REQUEST_JSON_MALFORMED charset / body 0',
            'REPORT REQUEST_JSON_MALFORMED inspection-incomplete / body 0']);
        });
      }).then(function () {
        var d = instance({ limits: { maxBodyBytes: 16 } });
        return send(d, { method: 'POST', url: '/json', headers: JSON_CT, body: '{"note":"more than sixteen bytes"}' }).then(function (r) {
          blocked(r, 'REQUEST_LIMIT_EXCEEDED');
          assert.deepStrictEqual(findingsOf(d), ['ENFORCE REQUEST_LIMIT_EXCEEDED max-body-bytes / body 0']);
        });
      }).then(function () {
        var q = instance({ mode: 'REPORT', limits: { maxBodyBytes: 16 } });
        return send(q, { method: 'POST', url: '/json', headers: JSON_CT, body: '{"note":"<b> more than sixteen"}' }).then(function (r) {
          assert.strictEqual(r.status, 200);
          assert.deepStrictEqual(findingsOf(q), ['REPORT REQUEST_LIMIT_EXCEEDED max-body-bytes / body 0',
            'REPORT REQUEST_LIMIT_EXCEEDED inspection-incomplete / body 0'], 'the body itself is not inspected');
        });
      }).then(function () {
        var z = instance();
        return send(z, { method: 'POST', url: '/json', headers: { 'content-type': 'application/json;charset=ISO-8859-1', 'content-length': '0' } });
      }).then(function (r) {
        assert.notStrictEqual(r.status, 400, 'an empty body never reaches the body checks (Spring skips the advice)');
      });
});

test('verify hook: an undeclared body over maxBodyBytes is max-body-bytes with length 0; empty bodies are skipped', function () {
  var e = instance({ limits: { maxBodyBytes: 16 } });
  return send(e, { method: 'POST', url: '/json', headers: { 'content-type': 'application/json', 'transfer-encoding': 'chunked' },
    body: '{"note":"more than sixteen bytes"}' }).then(function (r) {
    blocked(r, 'REQUEST_LIMIT_EXCEEDED');
    assert.deepStrictEqual(findingsOf(e), ['ENFORCE REQUEST_LIMIT_EXCEEDED max-body-bytes / body 0']);
    var z = instance();
    return send(z, { method: 'POST', url: '/json', headers: { 'content-type': 'application/json', 'transfer-encoding': 'chunked' }, body: '' });
  }).then(function (r) {
    assert.strictEqual(r.status, 200);
  });
});

test('path parameters: name then value, kind path, same budget; skipped after a REPORT scalar limit', function () {
  var e = instance();
  return send(e, { url: '/path/%3Cb%3E' }).then(function (r) {
    blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
    assert.deepStrictEqual(findingsOf(e), ['ENFORCE REQUEST_CONTENT_NOT_ALLOWED R1 id path 3']);
    var p = instance({ mode: 'REPORT' });
    return send(p, { url: '/two/javascript%3Ax/%3Ci%3E?q=ok' }).then(function (r2) {
      assert.strictEqual(r2.status, 200);
      assert.deepStrictEqual(findingsOf(p), ['REPORT REQUEST_CONTENT_NOT_ALLOWED R2 a path 12', 'REPORT REQUEST_CONTENT_NOT_ALLOWED R1 b path 3']);
    });
  }).then(function () {
    var s = instance({ mode: 'REPORT', limits: { maxScalarLength: 20 } });
    return send(s, { url: '/path/%3Cb%3E?a=xxxxxxxxxxxxxxxxxxxxx' }).then(function () {
      assert.deepStrictEqual(findingsOf(s), ['REPORT REQUEST_LIMIT_EXCEEDED scalar-limit a query/form 21',
        'REPORT REQUEST_LIMIT_EXCEEDED inspection-incomplete / query/form 0']);
    });
  }).then(function () {
    // Express 5 wildcard parameters arrive as arrays; each string element is inspected
    var t = instance();
    var req = mini.createRequest({ url: '/files/a/b' });
    req.params = { path: ['a', '<b>'] };
    var res = mini.createResponse(function () {});
    var nextCalled = false;
    t.rv.pathParams(req, res, function () {
      nextCalled = true;
    });
    assert.strictEqual(nextCalled, false);
    assert.strictEqual(res.statusCode, 400);
  });
});

test('error arm: transport failures on a JSON body; other errors and REPORT pass through unchanged', function () {
  var e = instance();
  return send(e, { method: 'POST', url: '/json', headers: JSON_CT, body: '{"a":"connection reset"}', abort: true }).then(function (r) {
    blocked(r, 'REQUEST_JSON_MALFORMED');
    assert.strictEqual(r.body.indexOf('aborted'), -1);
    assert.deepStrictEqual(findingsOf(e), ['ENFORCE REQUEST_JSON_MALFORMED body-read / body 0']);
    var p = instance({ mode: 'REPORT' });
    return send(p, { method: 'POST', url: '/json', headers: JSON_CT, body: '{"a":1}', abort: true }).then(function (r2) {
      assert.strictEqual(r2.status, 400);
      assert.strictEqual(r2.error.type, 'request.aborted', 'the original error reaches the host');
      assert.deepStrictEqual(findingsOf(p), ['REPORT REQUEST_JSON_MALFORMED inspection-incomplete / transport 0']);
    });
  }).then(function () {
    var f = instance();
    return send(f, { method: 'POST', url: '/form?q=%3Cb%3E', headers: { 'content-type': 'application/x-www-form-urlencoded; charset=koi8-r' }, body: 'a=1' });
  }).then(function (r) {
    blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
    var g = instance({ mode: 'REPORT' });
    return send(g, { method: 'POST', url: '/json', headers: JSON_CT, body: '{"a":' }).then(function (r2) {
      assert.strictEqual(r2.status, 400);
      assert.strictEqual(r2.error.type, 'entity.parse.failed', 'REPORT never changes the host answer');
    });
  });
});

test('error arm: a RequestValidationError is rendered by brand; with the response already started it is passed on', function () {
  var t = instance();
  var err = new rv.RequestValidationError('REQUEST_JSON_DUPLICATE_KEY');
  err.type = 'entity.verify.failed';
  err.body = Buffer.from('{"a":1,"a":2}');
  var res = mini.createResponse(function () {});
  t.rv.afterParsers[1](err, mini.createRequest({}), res, function () {
    throw new Error('next must not be called');
  });
  assert.strictEqual(res.statusCode, 400);
  assert.ok(res.body.toString().indexOf('REQUEST_JSON_DUPLICATE_KEY') !== -1);
  assert.strictEqual(res.body.toString().indexOf('"a":1'), -1);
  var sent = mini.createResponse(function () {});
  sent.headersSent = true;
  var passed = null;
  t.rv.afterParsers[1](err, mini.createRequest({}), sent, function (e) {
    passed = e;
  });
  assert.strictEqual(passed, err);
});

test('safety net: a parsed body whose hook never ran is walked once, with one WARN per parser (D20)', function () {
  var t = instance({ mode: 'REPORT' });
  var app = new mini.App();
  app.use(t.rv.beforeParsers);
  app.use(mini.bodyParser('json', {}));
  app.use(mini.bodyParser('urlencoded', {}));
  app.use(t.rv.afterParsers);
  app.use(mini.echo);
  return mini.request(app, { method: 'POST', url: '/x', headers: JSON_CT, body: '{"a":["ok",{"<k>":"javascript:1"}],"n":5}' }).then(function (r) {
    assert.strictEqual(r.status, 200);
    return mini.request(app, { method: 'POST', url: '/x', headers: JSON_CT, body: '{"b":"<i>"}' });
  }).then(function () {
    return mini.request(app, { method: 'POST', url: '/x', headers: FORM_CT, body: 'f=%3Cb%3E' });
  }).then(function () {
    var warnings = t.logger.lines.filter(function (l) {
      return l.line.indexOf('request_validation_hook_missing') === 0;
    }).map(function (l) {
      return l.line;
    });
    assert.deepStrictEqual(warnings, ['request_validation_hook_missing parser=json', 'request_validation_hook_missing parser=urlencoded']);
    assert.deepStrictEqual(findingsOf(t), ['REPORT REQUEST_CONTENT_NOT_ALLOWED R1 /a/1/* body 3',
      'REPORT REQUEST_CONTENT_NOT_ALLOWED R2 /a/1/* body 12', 'REPORT REQUEST_CONTENT_NOT_ALLOWED R1 /b body 3',
      'REPORT REQUEST_CONTENT_NOT_ALLOWED R1 f query/form 3']);
  });
});

test('safety net: a JSON-typed body read by a raw parser (bytes) logs the hook warning and is not walked', function () {
  var t = instance({ mode: 'ENFORCE' });
  var app = new mini.App();
  app.use(t.rv.beforeParsers);
  app.use(mini.bodyParser('json', { parse: 'raw' }));
  var afterMs = null;
  app.use(function (req, res, next) {
    var t0 = Date.now();
    t.rv.afterParsers[0](req, res, function (err) {
      afterMs = Date.now() - t0;
      next(err);
    });
  });
  app.use(mini.echo);
  var big = '{"a":"<b>","pad":"' + 'x'.repeat(1024 * 1024) + '"}';
  return mini.request(app, { method: 'POST', url: '/hook', headers: JSON_CT, body: big }).then(function (r) {
    assert.strictEqual(r.status, 200);
    assert.strictEqual(Buffer.from(JSON.parse(r.body).body, 'base64').toString('utf8'), big);
    assert.deepStrictEqual(t.logger.lines.map(function (l) {
      return l.line;
    }), ['request_validation_hook_missing parser=json']);
    assert.ok(afterMs !== null && afterMs < 250, 'afterParsers took ' + afterMs + ' ms on a 1 MiB raw body');
  });
});

test('without beforeParsers: state is created by the hook and the scalars run in afterParsers', function () {
  var t = instance({ mode: 'REPORT' });
  var app = new mini.App();
  app.use(mini.bodyParser('json', { verify: t.rv.jsonVerify }));
  app.use(t.rv.afterParsers);
  app.use(mini.echo);
  return mini.request(app, { method: 'POST', url: '/x?q=%3Cb%3E', headers: JSON_CT, body: '{"a":"<i>"}' }).then(function (r) {
    assert.strictEqual(r.status, 200);
    assert.deepStrictEqual(findingsOf(t), ['REPORT REQUEST_CONTENT_NOT_ALLOWED R1 /a body 3', 'REPORT REQUEST_CONTENT_NOT_ALLOWED R1 q query/form 3']);
  });
});

test('routes: exclusions, per-route mode, structured:false keeps scalar checks, ANNOTATED ignores other paths', function () {
  var t = instance({ mode: 'REPORT', excludePaths: [{ path: '/tracing', reason: 'proxy' }],
    routes: [{ path: '/strict', mode: 'ENFORCE' }, { path: '/skip', structured: false, reason: 'raw body' }] });
  return send(t, { url: '/tracing/api?q=%3Cscript%3E' }).then(function (r) {
    assert.strictEqual(r.status, 200);
    return send(t, { method: 'POST', url: '/strict', headers: JSON_CT, body: '{"a":"<b>"}' });
  }).then(function (r) {
    blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
    return send(t, { method: 'POST', url: '/json', headers: JSON_CT, body: '{"a":"<b>"}' });
  }).then(function (r) {
    assert.strictEqual(r.status, 200);
    var e = instance({ routes: [{ path: '/skip', structured: false, reason: 'raw body' }] });
    return send(e, { method: 'POST', url: '/skip?count=1', headers: JSON_CT, body: '"<script>"' }).then(function (r2) {
      assert.strictEqual(r2.status, 200);
      return send(e, { method: 'POST', url: '/skip?count=%3Cscript%3E', headers: JSON_CT, body: '{}' });
    }).then(function (r2) {
      blocked(r2, 'REQUEST_CONTENT_NOT_ALLOWED');
    });
  }).then(function () {
    var a = instance({ activation: 'ANNOTATED', routes: [{ path: '/bytes', method: 'POST' }] });
    return send(a, { method: 'POST', url: '/plain', headers: JSON_CT, body: '"<script>"' }).then(function (r) {
      assert.strictEqual(r.status, 200);
      return send(a, { method: 'POST', url: '/bytes', headers: JSON_CT, body: '"<script>"' });
    }).then(function (r) {
      blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
    });
  });
});

test('internal errors: REPORT fails open and logs once; ENFORCE fails closed with 500, also from a verify hook', function () {
  var hostile = {};
  Object.defineProperty(hostile, 'toString', { value: function () {
    throw new Error('<secret>');
  } });
  var p = instance({ mode: 'REPORT' });
  var req = mini.createRequest({ url: '/q?x=1', headers: {} });
  req.headers['content-type'] = hostile;
  var res = mini.createResponse(function () {});
  var calls = 0;
  p.rv.beforeParsers(req, res, function () {
    calls++;
  });
  p.rv.afterParsers[0](req, res, function () {
    calls++;
  });
  assert.strictEqual(calls, 2);
  assert.deepStrictEqual(p.logger.lines.map(function (l) {
    return l.level + ' ' + l.line;
  }), ['error request_validation_internal_error phase=before error=Error']);
  var e = instance();
  var req2 = mini.createRequest({ url: '/q', headers: {} });
  req2.headers['content-type'] = hostile;
  var res2 = mini.createResponse(function () {});
  e.rv.beforeParsers(req2, res2, function () {
    throw new Error('next must not be called');
  });
  assert.strictEqual(res2.statusCode, 500);
  assert.ok(res2.body.toString().indexOf('"code":"INTERNAL_SERVER_ERROR"') !== -1);
  var req3 = mini.createRequest({ method: 'POST', url: '/json', headers: JSON_CT });
  assert.throws(function () {
    e.rv.jsonVerify(req3, mini.createResponse(function () {}), { length: 3 }, 'utf-8');
  }, function (err) {
    return rv.isRequestValidationError(err) && err.status === 500;
  });
  var req4 = mini.createRequest({ method: 'POST', url: '/json', headers: JSON_CT });
  p.rv.jsonVerify(req4, mini.createResponse(function () {}), { length: 3 }, 'utf-8');
});

test('inspectJsonBuffer: JSON read by the service (gzip); ENFORCE throws a 400 error, REPORT returns', function () {
  var e = instance();
  var req = mini.createRequest({ method: 'POST', url: '/gz', headers: { 'content-type': 'application/gzip' } });
  e.rv.beforeParsers(req, mini.createResponse(function () {}), function () {});
  assert.throws(function () {
    e.rv.inspectJsonBuffer(req, Buffer.from('{"a":"<script>"}'));
  }, function (err) {
    return rv.isRequestValidationError(err) && err.status === 400 && err.code === 'REQUEST_CONTENT_NOT_ALLOWED'
        && err.message === 'Request contains content that is not allowed';
  });
  var p = instance({ mode: 'REPORT' });
  var req2 = mini.createRequest({ method: 'POST', url: '/gz', headers: { 'content-type': 'application/gzip' } });
  p.rv.inspectJsonBuffer(req2, Buffer.from('{"a":"<script>"}'));
  p.rv.inspectJsonBuffer(req2, Buffer.from('{"b":"<script>"}'));
  assert.deepStrictEqual(findingsOf(p), ['REPORT REQUEST_CONTENT_NOT_ALLOWED R1 /a body 8'], 'one body per request');
  assert.throws(function () {
    p.rv.inspectJsonBuffer(req2, '{"a":1}');
  }, TypeError);
});

test('time budget: maxInspectionMillis stops a marker-heavy body with inspection-budget; slow inspections WARN', function () {
  var parts = [];
  for (var i = 0; i < 20000; i++) {
    parts.push('"&amp;&#37;%2525&lt;' + i + '"');
  }
  var body = '[' + parts.join(',') + ']';
  var e = instance({ maxInspectionMillis: 1, slowInspectionWarnMillis: 1 });
  return send(e, { method: 'POST', url: '/json', headers: JSON_CT, body: body }).then(function (r) {
    blocked(r, 'REQUEST_LIMIT_EXCEEDED');
    var f = e.logger.findings();
    assert.strictEqual(f.length, 1);
    assert.strictEqual(f[0].rule, 'inspection-budget');
    assert.strictEqual(f[0].length, '0');
    assert.ok(e.logger.lines.some(function (l) {
      return /^request_validation_slow_inspection phase=json ms=\d+ bytes=\d+ handler=\/json$/.test(l.line);
    }));
    var p = instance({ mode: 'REPORT', maxInspectionMillis: 1 });
    return send(p, { method: 'POST', url: '/json', headers: JSON_CT, body: body });
  }).then(function (r) {
    assert.strictEqual(r.status, 200);
  });
});

test('no mutation: req.body, query, params, headers, Object.keys(req) and the verify buffer are unchanged', function () {
  function run(withPackage) {
    var t = instance({ mode: 'REPORT' });
    var noop = rv.createRequestValidation({ env: false });
    var used = withPackage ? t.rv : noop;
    var hashes = [];
    var app = new mini.App();
    app.use(used.beforeParsers);
    app.use(mini.bodyParser('json', { verify: function (req, res, buf, enc) {
      var before = crypto.createHash('sha256').update(buf).digest('hex');
      used.jsonVerify(req, res, buf, enc);
      hashes.push(before === crypto.createHash('sha256').update(buf).digest('hex'));
    } }));
    app.use(mini.bodyParser('urlencoded', { verify: used.formVerify }));
    app.use(used.afterParsers);
    var seen = null;
    app.route('POST', '/p/:id', used.pathParams, function (req, res) {
      seen = { body: req.body, params: req.params, headers: req.headers, keys: Object.keys(req).sort() };
      mini.echo(req, res);
    });
    var url = '/p/%3Cb%3E?q=%3Cscript%3E&x=%zz';
    return Promise.all([
      mini.request(app, { method: 'POST', url: url, headers: JSON_CT, body: '{"a":"<b>","b":[1,"javascript:x"]}' }).then(function () {
        return { seen: seen, hashes: hashes.slice() };
      })
    ]).then(function (all) {
      return all[0];
    });
  }
  return Promise.all([run(true), run(false)]).then(function (pair) {
    var a = pair[0];
    var b = pair[1];
    assert.deepStrictEqual(a.seen.body, b.seen.body);
    assert.deepStrictEqual(a.seen.params, b.seen.params);
    assert.deepStrictEqual(a.seen.headers, b.seen.headers);
    assert.deepStrictEqual(a.seen.keys, b.seen.keys);
    assert.deepStrictEqual(a.hashes, [true]);
  });
});

test('Express 5 request objects: a read-only req.query getter and an undefined req.body are never touched', function () {
  var t = instance({ mode: 'REPORT' });
  var req = mini.createRequest({ url: '/q?x=%3Cb%3E' });
  var reads = 0;
  Object.defineProperty(req, 'query', { get: function () {
    reads++;
    return { x: '<b>' };
  }, enumerable: true, configurable: false });
  req.body = undefined;
  var res = mini.createResponse(function () {});
  var calls = 0;
  t.rv.beforeParsers(req, res, function () {
    calls++;
  });
  t.rv.afterParsers[0](req, res, function () {
    calls++;
  });
  assert.strictEqual(calls, 2);
  assert.strictEqual(reads, 0);
  assert.strictEqual(req.body, undefined);
  assert.deepStrictEqual(findingsOf(t), ['REPORT REQUEST_CONTENT_NOT_ALLOWED R1 x query/form 3']);
});

test('scalar budget: the count is bounded by maxTokens and the characters by maxBodyBytes (Content-Type included)', function () {
  // Java (Spring Boot 2.2 service, maxTokens 5 and maxBodyBytes 16): scalar-limit at c, and at a, respectively
  var t = instance({ limits: { maxTokens: 5 } });
  return send(t, { method: 'POST', url: '/json?a=1&b=2&c=3', headers: JSON_CT, body: '{}' }).then(function (r) {
    blocked(r, 'REQUEST_LIMIT_EXCEEDED');
    assert.deepStrictEqual(findingsOf(t), ['ENFORCE REQUEST_LIMIT_EXCEEDED scalar-limit c query/form 1']);
    var b = instance({ limits: { maxBodyBytes: 16 } });
    return send(b, { method: 'POST', url: '/json?a=1', headers: JSON_CT, body: '{}' }).then(function (r2) {
      blocked(r2, 'REQUEST_LIMIT_EXCEEDED');
      assert.deepStrictEqual(findingsOf(b), ['ENFORCE REQUEST_LIMIT_EXCEEDED scalar-limit a query/form 1']);
    });
  }).then(function () {
    var ok = instance({ limits: { maxTokens: 5 } });
    return send(ok, { method: 'POST', url: '/json?a=1&b=2', headers: JSON_CT, body: '{}' }).then(function (r) {
      assert.strictEqual(r.status, 200, 'exactly the limit passes');
    });
  });
});

test('log lines never contain request text: markers in values, unsafe names, paths and the Content-Type header', function () {
  var marker = 'zq9marker';
  var t = instance({ mode: 'REPORT', logContext: function () {
    return { cid: 'fixed' };
  } });
  var body = '{"' + marker + ' <x>":"<' + marker + '>","a":["javascript:' + marker + '"],"b":"' + marker + '\\u0000"}';
  return send(t, { method: 'POST', url: '/path/%3C' + marker + '%3E?' + marker + '%20=%3C' + marker + '%3E&q=%3Cb' + marker + '%zz',
    headers: { 'content-type': 'application/json; x="<' + marker + '>"' }, body: body }).then(function () {
    return send(t, { method: 'POST', url: '/form', headers: FORM_CT, body: 'f=%3C' + marker + '%3E&' + marker + '<=1' });
  }).then(function () {
    var lines = t.logger.lines.map(function (l) {
      return l.line;
    });
    assert.ok(t.logger.findings().length >= 6, 'findings were logged: ' + lines.length);
    lines.forEach(function (line) {
      assert.strictEqual(line.indexOf(marker), -1, line);
    });
  });
});

test('stream consumers after the middlewares (multipart, raw handlers) still receive every byte', function () {
  var t = instance({ mode: 'ENFORCE' });
  var payload = Buffer.from('--b\r\nContent-Disposition: form-data; name="f"; filename="a.html"\r\n\r\n<script>x</script>\r\n--b--\r\n');
  var got = null;
  var app = new mini.App();
  app.use(t.rv.beforeParsers);
  app.use(mini.bodyParser('json', { verify: t.rv.jsonVerify }));
  app.use(mini.bodyParser('urlencoded', { verify: t.rv.formVerify }));
  app.use(t.rv.afterParsers);
  app.use(function (req, res) {
    var chunks = [];
    req.on('data', function (c) {
      chunks.push(c);
    });
    req.on('end', function () {
      got = Buffer.concat(chunks);
      res.statusCode = 200;
      res.end('ok');
    });
  });
  return mini.request(app, { method: 'POST', url: '/upload', headers: { 'content-type': 'multipart/form-data; boundary=b' }, body: payload })
      .then(function (r) {
        assert.strictEqual(r.status, 200);
        assert.ok(got.equals(payload));
        assert.deepStrictEqual(findingsOf(t), []);
      });
});

test('mount order: the error middleware must come after the parsers to render a verify-hook rejection', function () {
  var t = instance();
  function build(afterFirst) {
    var app = new mini.App();
    app.use(t.rv.beforeParsers);
    if (afterFirst) {
      app.use(t.rv.afterParsers);
    }
    app.use(mini.bodyParser('json', { verify: t.rv.jsonVerify }));
    if (!afterFirst) {
      app.use(t.rv.afterParsers);
    }
    app.use(mini.echo);
    return app;
  }
  var request = { method: 'POST', url: '/json', headers: JSON_CT, body: '{"a":"<b>"}' };
  return mini.request(build(false), request).then(function (r) {
    blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
    return mini.request(build(true), request);
  }).then(function (r) {
    // mounted too early, the rejection reaches the host's error handling instead of the package's renderer
    assert.strictEqual(r.status, 400);
    assert.ok(rv.isRequestValidationError(r.error));
    assert.strictEqual(r.headers.connection, undefined);
  });
});
