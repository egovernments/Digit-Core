'use strict';

// Route policies are resolved on the path Express routes the request on (parseurl: the target before '?', or
// url.parse()'s pathname for a target with a fragment, whitespace, U+00A0 or U+FEFF, or not starting with '/'), with
// Express's case-insensitive matching.

var assert = require('assert');
var path = require('path');
var childProcess = require('child_process');
var h = require('./harness');
var test = h.test;
var rv = require(path.join(h.ROOT, 'index.js'));
var requestTarget = require(path.join(h.ROOT, 'lib', 'web', 'requestTarget'));
var resolve = require(path.join(h.ROOT, 'lib', 'config', 'resolve'));
var mini = require('./support/miniapp');

test('routed path: the target before "?", or the url.parse pathname', function () {
  var cases = [
    ['/v1/_create', '/v1/_create'], ['/v1/_create?a=1#b', '/v1/_create'], ['/v1/_create#x', '/v1/_create'],
    ['/v1/_create#', '/v1/_create'], ['/v1\\_create#', '/v1/_create'], ['/v1\\_create', '/v1\\_create'],
    ['/v1/_create\u00A0', '/v1/_create'], ['/v1/_create\uFEFF', '/v1/_create'], ['/v1/_create\t', '/v1/_create'],
    ['/a b#c', '/a%20b'], ['/a#b?c', '/a'], ['/a?b#c', '/a'], ['/a<b>#', '/a%3Cb%3E'], ['/x?q=1 2', '/x'],
    ['http://host/v1/_create?q', '/v1/_create'], ['http://host', '/'], ['//u@h/p#', '/p'], ['', null], ['?a', null]
  ];
  cases.forEach(function (c) {
    assert.strictEqual(requestTarget.routedPathOf(c[0]), c[1], JSON.stringify(c[0]));
  });
});

test('routed path: equals parseurl over url.parse of this runtime on 20,000 request targets', function () {
  var script = [
    'var url = require("url");',
    'var rt = require(' + JSON.stringify(path.join(h.ROOT, 'lib', 'web', 'requestTarget')) + ');',
    'function real(s) { if (s.charCodeAt(0) !== 0x2f) return url.parse(s).pathname;',
    '  for (var i = 1; i < s.length; i++) { var c = s.charCodeAt(i);',
    '    if (c === 9 || c === 10 || c === 12 || c === 13 || c === 32 || c === 35 || c === 0xa0) return url.parse(s).pathname; }',
    '  var q = s.indexOf("?"); return q === -1 ? s : s.slice(0, q); }',
    'var A = ["/", "/", "\\\\", "#", "?", "@", ":", " ", "\\t", "\\n", "\\r", "\\f", "\\u000b", "\\u00a0", "%", "<", "\\"",',
    '  "\'", "^", "`", "{", "|", "}", ";", "[", "]", "a", "Z", "v1", "_c", "0", ".", "-", "+", "\\u00e9", "\\u00ff", "&", "="];',
    'var P = ["/", "//", "///", "", "http://", "HTTP://", "https://h", "x:", "//u@h", "\\\\", " /", "http://u@h:80",',
    '  "http://[::1]", "ftp:", "*", "?", "#"];',
    'var seed = 7, bad = [], n = 0, threw = 0;',
    'function rnd(k) { seed = (Math.imul(seed, 1103515245) + 12345) >>> 0; return (seed >>> 8) % k; }',
    'for (var i = 0; i < 20000; i++) { var s = P[rnd(P.length)]; var len = rnd(9);',
    '  for (var k = 0; k < len; k++) s += A[rnd(A.length)];',
    '  var r; try { r = real(s); } catch (e) { threw++; continue; } n++;',
    '  if (r === undefined) r = null; var m = rt.routedPathOf(s);',
    '  if (m !== r && bad.length < 5) bad.push([s, r, m]); }',
    'process.stdout.write(JSON.stringify({ compared: n, threw: threw, mismatches: bad }));'
  ].join('\n');
  var r = childProcess.spawnSync(process.execPath, ['--no-deprecation', '-e', script], { encoding: 'utf8' });
  assert.strictEqual(r.status, 0, r.stderr);
  var out = JSON.parse(r.stdout);
  assert.deepStrictEqual(out.mismatches, []);
  assert.ok(out.compared > 15000, JSON.stringify(out));
});

test('routed path: the router\'s own parse of the target is used when it is on the request', function () {
  var req = { url: '/a#b', originalUrl: '/a#b', _parsedUrl: { _raw: '/a#b', pathname: '/from-router', query: 'x=1' } };
  assert.deepStrictEqual(requestTarget.routedTarget(req, '/a#b'), { pathname: '/from-router', query: 'x=1' });
  req._parsedUrl._raw = '/other';
  assert.deepStrictEqual(requestTarget.routedTarget(req, '/a#b'), { pathname: '/a', query: null });
  var mounted = { url: '/b', originalUrl: '/api/b?q=1', _parsedOriginalUrl: { _raw: '/api/b?q=1', pathname: '/api/b', query: 'q=1' } };
  assert.deepStrictEqual(requestTarget.routedTarget(mounted, '/api/b?q=1'), { pathname: '/api/b', query: 'q=1' });
});

test('route matching ignores case as Express does, non-ASCII letters included', function () {
  assert.strictEqual(resolve.normalizePath('/V1/_Create/'), resolve.normalizePath('/v1/_create'));
  assert.strictEqual(resolve.normalizePath('/CAF\u00C9'), resolve.normalizePath('/caf\u00E9'));
  assert.strictEqual(resolve.normalizePath('/\u00B5'), resolve.normalizePath('/\u039C'), 'micro sign and capital mu');
  // A unit above U+007F never matches an ASCII letter, except where this runtime's regular expressions fold it.
  ['\u017F', '\u212A', '\u0131'].forEach(function (u) {
    var regexFolds = new RegExp('^' + u + '$', 'i').test(u === '\u212A' ? 'k' : u === '\u017F' ? 's' : 'i');
    var ascii = u === '\u212A' ? 'k' : u === '\u017F' ? 's' : 'i';
    assert.strictEqual(resolve.normalizePath('/' + u) === resolve.normalizePath('/' + ascii), regexFolds, h.unitsHex(u));
  });
});

function instance(options) {
  var logger = mini.recordingLogger();
  var r = rv.createRequestValidation(Object.assign({ env: false, enabled: true, structuredDefault: true, logger: logger },
      options));
  logger.clear();
  return { rv: r, logger: logger, app: mini.serviceApp(r) };
}

test('a fragment, backslash, trailing U+00A0 or other case cannot select another route policy', function () {
  var targets = ['/v1/_create', '/v1/_create#x', '/v1/_create#', '/v1\\_create#', '/v1/_create\u00A0', '/V1/_CREATE#x',
    '/v1/_create?#'];
  var configs = [
    { activation: 'ANNOTATED', mode: 'ENFORCE', routes: [{ path: '/v1/_create', method: 'POST' }] },
    { activation: 'ALL', mode: 'REPORT', routes: [{ path: '/v1/_create', method: 'POST', mode: 'ENFORCE' }] }
  ];
  var runs = [];
  configs.forEach(function (config) {
    targets.forEach(function (target) {
      runs.push(function () {
        var t = instance(config);
        return mini.request(t.app, { method: 'POST', url: target, headers: { 'content-type': 'application/json' },
          body: '{"a":"<script>alert(1)</script>"}' }).then(function (r) {
          assert.strictEqual(r.status, 400, JSON.stringify(target) + ' ' + JSON.stringify(config));
          assert.strictEqual(r.handled, false);
          assert.ok(t.logger.lines[0].line.indexOf(' handler=POST /v1/_create ') !== -1, t.logger.lines[0].line);
        });
      });
    });
  });
  var t2 = instance({ activation: 'ANNOTATED', mode: 'ENFORCE', routes: [{ path: '/caf\u00E9' }] });
  runs.push(function () {
    return mini.request(t2.app, { url: '/CAF\u00C9?q=%3Cb%3E' }).then(function (r) {
      assert.strictEqual(r.status, 400);
    });
  });
  return runs.reduce(function (p, run) {
    return p.then(run);
  }, Promise.resolve());
});
