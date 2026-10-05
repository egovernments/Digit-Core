'use strict';

// Urlencoded bodies within and past the servlet container's limits (10,000 parameters, 2 MiB), the time budget while
// a form is parsed, a urlencoded body taken by a text parser, and multipart text fields (multipartFields). Every
// request that is answered 200 is also checked for what the handler received.

var assert = require('assert');
var path = require('path');
var h = require('./harness');
var test = h.test;
var rv = require(path.join(h.ROOT, 'index.js'));
var mini = require('./support/miniapp');

var FORM_CT = { 'content-type': 'application/x-www-form-urlencoded' };

function instance(extra) {
  var logger = mini.recordingLogger();
  var r = rv.createRequestValidation(Object.assign({ env: false, enabled: true, structuredDefault: true, activation: 'ALL',
    mode: 'ENFORCE', logger: logger }, extra || {}));
  logger.clear();
  return { rv: r, logger: logger };
}

function findingsOf(t) {
  return t.logger.findings().map(function (f) {
    return f.mode + ' ' + f.code + ' ' + f.rule + ' ' + f.location + ' ' + f.kind + ' ' + f.length;
  });
}

function formApp(t, parserOptions, extraRoutes) {
  var app = new mini.App();
  app.use(t.rv.beforeParsers);
  app.use(mini.bodyParser('urlencoded', Object.assign({ verify: t.rv.formVerify }, parserOptions || {})));
  app.use(t.rv.afterParsers);
  (extraRoutes || []).forEach(function (route) {
    app.route.apply(app, route);
  });
  app.use(mini.echo);
  return app;
}

function blocked(r, code) {
  assert.strictEqual(r.status, 400);
  assert.strictEqual(r.handled, false);
  assert.strictEqual(JSON.parse(r.body).Errors[0].code, code);
}

// A 200 answer must not carry the probe text in what the handler received.
function delivered(r, probe) {
  assert.strictEqual(r.status, 200);
  assert.strictEqual(r.handled, true);
  assert.strictEqual(r.body.indexOf(probe), -1, 'the handler received ' + probe);
}

function pairs(n, at, value) {
  var out = [];
  for (var i = 0; i < n; i++) {
    out.push('p' + i + '=' + (i === at ? value : '1'));
  }
  return out.join('&');
}

test('form past 10,000 parameters: markup in the first 10,000 is found in formVerify, later markup after the parser', function () {
  var early = instance();
  return mini.request(formApp(early), { method: 'POST', url: '/f', headers: FORM_CT, body: pairs(10010, 9999, '%3Cb%3E') })
      .then(function (r) {
        blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
        assert.deepStrictEqual(findingsOf(early), ['ENFORCE REQUEST_CONTENT_NOT_ALLOWED R1 p9999 query/form 3']);
        var late = instance();
        return mini.request(formApp(late), { method: 'POST', url: '/f', headers: FORM_CT, body: pairs(10010, 10005, '%3Cb%3E') })
            .then(function (r2) {
              blocked(r2, 'REQUEST_CONTENT_NOT_ALLOWED');
              assert.deepStrictEqual(findingsOf(late), ['ENFORCE REQUEST_CONTENT_NOT_ALLOWED R1 p10005 query/form 3']);
            });
      }).then(function () {
        // the parser rejects the body (parameterLimit): the parameters past the limit are never inspected
        var rejected = instance();
        return mini.request(formApp(rejected, { parameterLimit: 1000 }), { method: 'POST', url: '/f', headers: FORM_CT,
          body: pairs(10010, 10005, '%3Cb%3E') }).then(function (r) {
          assert.strictEqual(r.status, 413);
          assert.strictEqual(r.error.type, 'parameters.too.many');
          assert.deepStrictEqual(findingsOf(rejected), []);
        });
      });
});

test('form over 2 MiB: not in the parameter map (as in Java), inspected after the parser accepted it', function () {
  var t = instance();
  var body = 'a=1&b=%3Cs%3E&c=' + 'x'.repeat(2 * 1024 * 1024);
  return mini.request(formApp(t), { method: 'POST', url: '/f?q=1', headers: FORM_CT, body: body }).then(function (r) {
    blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
    assert.deepStrictEqual(findingsOf(t), ['ENFORCE REQUEST_CONTENT_NOT_ALLOWED R1 b query/form 3']);
    var clean = instance();
    return mini.request(formApp(clean), { method: 'POST', url: '/f?q=1', headers: FORM_CT, body: 'a=1&c=' + 'x'.repeat(2 * 1024 * 1024) });
  }).then(function (r) {
    assert.strictEqual(r.status, 400, 'the 2 MiB value is over maxScalarLength');
    assert.strictEqual(JSON.parse(r.body).Errors[0].code, 'REQUEST_LIMIT_EXCEEDED');
  });
});

test('maxInspectionMillis also bounds the parameters of a large urlencoded body', function () {
  var t = instance({ mode: 'REPORT', maxInspectionMillis: 1 });
  var body = new Array(700001).join('ab=cd&');
  return mini.request(formApp(t), { method: 'POST', url: '/f', headers: FORM_CT, body: body }).then(function (r) {
    assert.strictEqual(r.status, 200);
    var found = t.logger.findings();
    assert.strictEqual(found.length, 2, JSON.stringify(found));
    assert.strictEqual(found[0].rule, 'inspection-budget');
    assert.strictEqual(found[1].rule, 'inspection-incomplete');
  });
});

test('a urlencoded body taken by a text parser without the hook is inspected as form parameters', function () {
  var t = instance();
  var app = new mini.App();
  app.use(t.rv.beforeParsers);
  app.use(mini.bodyParser('urlencoded', { parse: 'text' }));
  app.use(t.rv.afterParsers);
  app.use(mini.echo);
  return mini.request(app, { method: 'POST', url: '/f?q=1', headers: FORM_CT, body: 'a=1&n=%3Cscript%3E' }).then(function (r) {
    blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
    assert.deepStrictEqual(findingsOf(t), ['ENFORCE REQUEST_CONTENT_NOT_ALLOWED R1 n query/form 8']);
    assert.ok(t.logger.lines.some(function (l) {
      return l.line === 'request_validation_hook_missing parser=urlencoded';
    }));
  });
});

var MP_CT = { 'content-type': 'multipart/form-data; boundary=B0' };

function multipart(fields) {
  var out = '';
  fields.forEach(function (f) {
    out += '--B0\r\nContent-Disposition: form-data; name="' + f[0] + '"' + (f[2] ? '; filename="' + f[2] + '"' : '')
        + '\r\n\r\n' + f[1] + '\r\n';
  });
  return out + '--B0--\r\n';
}

function multipartApp(t, appLevel) {
  var app = new mini.App();
  app.use(t.rv.beforeParsers);
  app.use(mini.bodyParser('json', { verify: t.rv.jsonVerify }));
  app.use(mini.bodyParser('urlencoded', { verify: t.rv.formVerify }));
  if (appLevel) {
    app.use(mini.multipartParser());
  }
  app.use(t.rv.afterParsers);
  app.route('POST', '/upload', mini.multipartParser(), t.rv.multipartFields, mini.echo);
  app.route('POST', '/unwired', mini.multipartParser(), mini.echo);
  app.use(mini.echo);
  return app;
}

test('multipartFields: text field names and values are inspected as query/form parameters; files are not', function () {
  var t = instance();
  return mini.request(multipartApp(t), { method: 'POST', url: '/upload', headers: MP_CT,
    body: multipart([['note', '<script>alert(1)</script>'], ['file', '<script>', 'a<b>.txt']]) }).then(function (r) {
    blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
    assert.deepStrictEqual(findingsOf(t), ['ENFORCE REQUEST_CONTENT_NOT_ALLOWED R1 note query/form 25']);
    var n = instance();
    return mini.request(multipartApp(n), { method: 'POST', url: '/upload', headers: MP_CT,
      body: multipart([['<b>x', 'ok']]) }).then(function (r2) {
      blocked(r2, 'REQUEST_CONTENT_NOT_ALLOWED');
      assert.deepStrictEqual(findingsOf(n), ['ENFORCE REQUEST_CONTENT_NOT_ALLOWED R1 * query/form 4']);
    });
  }).then(function () {
    var c = instance();
    return mini.request(multipartApp(c), { method: 'POST', url: '/upload', headers: MP_CT,
      body: multipart([['note', 'hello'], ['note', 'world'], ['file', '<script>', 'x.txt']]) }).then(function (r) {
      assert.strictEqual(r.status, 200);
      assert.deepStrictEqual(JSON.parse(r.body).body, { note: ['hello', 'world'] }, 'fields reach the handler unchanged');
      assert.deepStrictEqual(findingsOf(c), []);
    });
  }).then(function () {
    var p = instance({ mode: 'REPORT' });
    return mini.request(multipartApp(p), { method: 'POST', url: '/upload', headers: MP_CT,
      body: multipart([['a', 'javascript:alert(1)'], ['b', 'x onclick=1']]) }).then(function (r) {
      assert.strictEqual(r.status, 200);
      assert.deepStrictEqual(findingsOf(p), ['REPORT REQUEST_CONTENT_NOT_ALLOWED R2 a query/form 19',
        'REPORT REQUEST_CONTENT_NOT_ALLOWED R3 b query/form 11']);
    });
  });
});

test('multipartFields: a field name already in the query is counted once, as in the servlet parameter map', function () {
  var t = instance({ limits: { maxTokens: 4 } });
  return mini.request(multipartApp(t), { method: 'POST', url: '/upload?a=1', headers: MP_CT, body: multipart([['a', '2']]) })
      .then(function (r) {
        assert.strictEqual(r.status, 200, 'Content-Type, a, 1, 2: four strings');
        var over = instance({ limits: { maxTokens: 4 } });
        return mini.request(multipartApp(over), { method: 'POST', url: '/upload?a=1', headers: MP_CT,
          body: multipart([['b', '2']]) });
      }).then(function (r) {
        blocked(r, 'REQUEST_LIMIT_EXCEEDED');
      });
});

test('multipart: a parser mounted before afterParsers is covered there; without multipartFields a route is not', function () {
  var t = instance();
  return mini.request(multipartApp(t, true), { method: 'POST', url: '/other', headers: MP_CT,
    body: multipart([['note', '<img src=x>']]) }).then(function (r) {
    blocked(r, 'REQUEST_CONTENT_NOT_ALLOWED');
    var u = instance();
    return mini.request(multipartApp(u), { method: 'POST', url: '/unwired', headers: MP_CT, body: multipart([['note', '<i>']]) });
  }).then(function (r) {
    assert.strictEqual(r.status, 200, 'documented: multipart fields are inspected only where multipartFields runs');
  });
});

test('multipartFields is a no-op of arity 3 when the package is disabled, and inert for other bodies', function () {
  var off = rv.createRequestValidation({ env: {} });
  assert.strictEqual(off.multipartFields.length, 3);
  var called = false;
  off.multipartFields({}, {}, function () {
    called = true;
  });
  assert.ok(called);
  var t = instance();
  assert.strictEqual(t.rv.multipartFields.length, 3);
  return mini.request(multipartApp(t), { method: 'POST', url: '/upload', headers: { 'content-type': 'application/json' },
    body: '{"note":"hello"}' }).then(function (r) {
    delivered(r, '<');
  });
});
