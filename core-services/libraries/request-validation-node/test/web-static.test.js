'use strict';

// Static checks for the web layer, the configuration and the entry points: Node 8.4 language level, no literal
// U+2028/U+2029, and no regular expression on request-derived text except /\+/g in the qs view (AC-19).

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
      walk(full, out);
    } else if (/\.js$/.test(name)) {
      out.push(full);
    }
  });
  return out;
}

function stripCommentsAndStrings(text) {
  return text.replace(/\/\*[\s\S]*?\*\//g, '')
      .replace(/'(?:\\.|[^'\\\n])*'/g, "''")
      .replace(/"(?:\\.|[^"\\\n])*"/g, '""')
      .replace(/\/\/.*$/gm, '');
}

var FILES = walk(path.join(h.ROOT, 'lib', 'web'), []).concat(walk(path.join(h.ROOT, 'lib', 'config'), []),
    [path.join(h.ROOT, 'lib', 'index.js'), path.join(h.ROOT, 'index.js')]);

test('lib/web, lib/config and the entry points use only the Node 8.4 language level', function () {
  var banned = [
    [/\?\./, 'optional chaining'], [/\?\?/, 'nullish coalescing'], [/\.\.\./, 'spread or rest'],
    [/\bcatch\s*\{/, 'optional catch binding'], [/\d_\d/, 'numeric separator'], [/\b\d+n\b/, 'BigInt literal'],
    [/\\p\{/, 'Unicode property escape'], [/\(\?<[=!a-zA-Z]/, 'lookbehind or named group'],
    [/\.flat\(|\.flatMap\(/, 'Array.prototype.flat'], [/Object\.(fromEntries|values|entries)/, 'ES2017+ Object helpers'],
    [/trimStart|trimEnd|matchAll|replaceAll|padStart|padEnd/, 'later string methods'], [/globalThis/, 'globalThis'],
    [/\.includes\(/, 'Array/String includes (kept out for uniformity)'], [/`/, 'template literal'],
    [/TextDecoder|\bisUtf8\(|AbortController|structuredClone|fs\/promises|require\(['"]node:/, 'APIs missing or noisy on Node 8']
  ];
  assert.ok(FILES.length >= 13, FILES.length + ' files');
  FILES.forEach(function (file) {
    var raw = fs.readFileSync(file, 'utf8');
    var rel = path.relative(h.ROOT, file);
    assert.strictEqual(raw.indexOf('\u2028'), -1, rel);
    assert.strictEqual(raw.indexOf('\u2029'), -1, rel);
    var code = stripCommentsAndStrings(raw);
    banned.forEach(function (rule) {
      assert.ok(!rule[0].test(code), rel + ': ' + rule[1]);
    });
    var inClass = false;
    code.split('\n').forEach(function (line) {
      if (/^class\s.*\{\s*$/.test(line)) {
        inClass = true;
      } else if (inClass && line === '}') {
        inClass = false;
      } else if (inClass) {
        assert.ok(!/^ {2}(static\s+)?#?[A-Za-z_$][\w$]*\s*(=|;)/.test(line), rel + ': class field: ' + line);
      }
    });
  });
});

test('no regular expression runs on request-derived text, except /\\+/g in the qs view', function () {
  FILES.forEach(function (file) {
    var code = stripCommentsAndStrings(fs.readFileSync(file, 'utf8'));
    var rel = path.relative(h.ROOT, file).split(path.sep).join('/');
    assert.ok(!/\bRegExp\b/.test(code), rel + ': RegExp');
    assert.ok(!/\.(match|search|exec|test)\(/.test(code), rel + ': regex method');
    var literals = code.match(/\.(replace|split)\(\s*\/[^\n]*?\/[gimsuy]*/g) || [];
    var allowed = {
      'lib/web/parameters.js': ['.replace(/\\+/g'],
      'lib/config/defaults.js': ['.replace(/[.-]/g']
    };
    assert.deepStrictEqual(literals, allowed[rel] || [], rel);
  });
  // the one in lib/config runs on setting names (constants) at module load, never on request data
  var defaultsSource = fs.readFileSync(path.join(h.ROOT, 'lib', 'config', 'defaults.js'), 'utf8');
  assert.ok(/property\.replace\(\/\[\.-\]\/g, '_'\)/.test(defaultsSource));
});

test('the package loads on this runtime without printing anything; disabled by default', function () {
  var script = 'var rv = require(' + JSON.stringify(path.join(h.ROOT, 'index.js')) + ');'
      + 'var x = rv.createRequestValidation({ env: {} });'
      + 'if (x.enabled !== false) { process.exit(3); }';
  var r = childProcess.spawnSync(process.execPath, ['-e', script], { encoding: 'utf8', env: {} });
  assert.strictEqual(r.status, 0, r.stderr);
  assert.strictEqual(r.stdout, '');
  assert.strictEqual(r.stderr, '');
});
