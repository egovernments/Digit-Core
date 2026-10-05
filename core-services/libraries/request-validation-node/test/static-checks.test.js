'use strict';

var assert = require('assert');
var fs = require('fs');
var path = require('path');
var childProcess = require('child_process');
var h = require('./harness');
var test = h.test;

function walk(dir, out) {
  fs.readdirSync(dir).sort().forEach(function (name) {
    var full = path.join(dir, name);
    if (fs.statSync(full).isDirectory()) {
      if (name !== 'node_modules' && name !== 'fixtures' && name.charAt(0) !== '.') {
        walk(full, out);
      }
    } else if (/\.(js|mjs)$/.test(name)) {
      out.push(full);
    }
  });
  return out;
}

// Removes comments and the contents of string literals so that only code is scanned.
function stripCommentsAndStrings(text) {
  return text.replace(/\/\*[\s\S]*?\*\//g, '')
      .replace(/'(?:\\.|[^'\\\n])*'/g, "''")
      .replace(/"(?:\\.|[^"\\\n])*"/g, '""')
      .replace(/\/\/.*$/gm, '');
}

var CORE = walk(path.join(h.ROOT, 'lib', 'core'), []);
var ALL_SOURCES = walk(h.ROOT, []);

test('no literal U+2028 or U+2029 in any source file (Node 8 rejects them in string literals)', function () {
  ALL_SOURCES.forEach(function (file) {
    var text = fs.readFileSync(file, 'utf8');
    assert.strictEqual(text.indexOf('\u2028'), -1, file);
    assert.strictEqual(text.indexOf('\u2029'), -1, file);
  });
});

test('lib/core uses only the Node 8.4 language level', function () {
  var banned = [
    [/\?\./, 'optional chaining'], [/\?\?/, 'nullish coalescing'], [/\.\.\./, 'spread or rest'],
    [/\bcatch\s*\{/, 'optional catch binding'], [/\d_\d/, 'numeric separator'], [/\b\d+n\b/, 'BigInt literal'],
    [/\\p\{/, 'Unicode property escape'], [/\(\?<[=!a-zA-Z]/, 'lookbehind or named group'],
    [/\.flat\(|\.flatMap\(/, 'Array.prototype.flat'], [/Object\.fromEntries/, 'Object.fromEntries'],
    [/trimStart|trimEnd|matchAll|replaceAll/, 'ES2019+ string methods'], [/globalThis/, 'globalThis'],
    [/TextDecoder|isUtf8|AbortController|structuredClone|fs\/promises/, 'APIs missing or noisy on Node 8']
  ];
  CORE.forEach(function (file) {
    var code = stripCommentsAndStrings(fs.readFileSync(file, 'utf8'));
    var rel = path.relative(h.ROOT, file);
    banned.forEach(function (rule) {
      assert.ok(!rule[0].test(code), rel + ': ' + rule[1]);
    });
    // Class fields: a member line of a class body (two-space indent) that is an assignment or bare declaration.
    var inClass = false;
    code.split('\n').forEach(function (line) {
      if (/^class\s.*\{\s*$/.test(line)) {
        inClass = true;
      } else if (inClass && line === '}') {
        inClass = false;
      } else if (inClass) {
        assert.ok(!/^ {2}(static\s+)?#?[A-Za-z_$][\w$]*\s*(=|;)/.test(line) && !/^ {2}#/.test(line), rel + ': class field: ' + line);
      }
    });
  });
});

test('lib/core applies no regular expression to request-derived text', function () {
  CORE.forEach(function (file) {
    var code = fs.readFileSync(file, 'utf8');
    var rel = path.relative(h.ROOT, file);
    assert.ok(!/\bRegExp\b/.test(code), rel + ': RegExp');
    assert.ok(!/\.(match|matchAll|search|exec|test)\(/.test(code), rel + ': regex method');
    assert.ok(!/\.(replace|split)\(\s*\//.test(code), rel + ': regex argument');
  });
});

test('node --check passes for every source file on this runtime; requiring lib/core prints nothing', function () {
  ALL_SOURCES.filter(function (file) {
    return /\.js$/.test(file);
  }).forEach(function (file) {
    var r = childProcess.spawnSync(process.execPath, ['--check', file], { encoding: 'utf8' });
    assert.strictEqual(r.status, 0, file + ': ' + r.stderr);
  });
  var script = CORE.map(function (file) {
    return 'require(' + JSON.stringify(file) + ');';
  }).join('');
  var r = childProcess.spawnSync(process.execPath, ['-e', script], { encoding: 'utf8' });
  assert.strictEqual(r.status, 0, r.stderr);
  assert.strictEqual(r.stderr, '');
  assert.strictEqual(r.stdout, '');
});

test('package.json is the specified manifest: no runtime dependencies, no install scripts', function () {
  var pkg = JSON.parse(fs.readFileSync(path.join(h.ROOT, 'package.json'), 'utf8'));
  assert.strictEqual(pkg.name, '@egovernments/request-validation');
  assert.strictEqual(pkg.version, '1.0.0');
  assert.deepStrictEqual(pkg.dependencies, {});
  assert.strictEqual(pkg.engines.node, '>=8.4.0');
  assert.deepStrictEqual(Object.keys(pkg.scripts), ['test']);
  assert.strictEqual(pkg.egovRequestValidation.conformsTo.jarSha256,
      'c3b55d47f25d4b027d0109c25dd61b30dda78339a3cb627613ce07004c7e85b4');
  assert.strictEqual(fs.existsSync(path.join(h.ROOT, '.git')), false);
});
