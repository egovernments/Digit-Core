'use strict';

var assert = require('assert');
var h = require('./harness');
var test = h.test;

var SkipPathMatcher = h.lib('SkipPathMatcher');
var REF = h.readJsonFixture('java-reference.json');

test('SkipPathMatcher: every Java matching case (exact path, descendants, wildcard, escapes, "/")', function () {
  var cases = REF.cases.filter(function (c) {
    return c.kind === 'path';
  });
  assert.strictEqual(cases.length, 11);
  cases.forEach(function (c) {
    var matcher = new SkipPathMatcher(c.config.skipPaths);
    assert.strictEqual(matcher.matches(c.input), c.expected, c.source + ' ' + JSON.stringify(c.input));
  });
});

test('SkipPathMatcher: every Java construction failure', function () {
  var cases = REF.cases.filter(function (c) {
    return c.kind === 'config' && c.source.indexOf('SkipPathMatcherTest#') === 0;
  });
  assert.strictEqual(cases.length, 4);
  cases.forEach(function (c) {
    assert.throws(function () { new SkipPathMatcher(c.input); }, RangeError, JSON.stringify(c.input));
  });
});

test('SkipPathMatcher: messages, escapes and type checks', function () {
  assert.throws(function () { new SkipPathMatcher(['']); }, /^RangeError: Empty skip path/);
  assert.throws(function () { new SkipPathMatcher(['a']); }, /must be a JSON Pointer/);
  assert.throws(function () { new SkipPathMatcher(['/a~']); }, /Invalid JSON Pointer escape/);
  assert.throws(function () { new SkipPathMatcher(['/a~2']); }, /Invalid JSON Pointer escape/);
  assert.throws(function () { new SkipPathMatcher(['/a*']); }, /Wildcard must occupy an entire path segment/);
  assert.throws(function () { new SkipPathMatcher(['/~0*']); }, /Wildcard/);
  assert.throws(function () { new SkipPathMatcher([null]); }, TypeError);
  assert.throws(function () { new SkipPathMatcher('/a'); }, TypeError);
  var m = new SkipPathMatcher(['/a~1b~0c/*', '/x//y']);
  assert.deepStrictEqual(m.patterns.map(function (p) { return p.slice(); }), [['a/b~c', '*'], ['x', '', 'y']]);
  assert.strictEqual(m.matches(['a/b~c', 'z']), true);
  assert.strictEqual(m.matches(['a/b~c']), false);
  assert.strictEqual(m.matches(['x', '', 'y', '0']), true);
  assert.strictEqual(m.matches(['x', 'y']), false);
  assert.strictEqual(new SkipPathMatcher(['/*']).matches(['anything']), true);
  assert.strictEqual(new SkipPathMatcher(['/*']).matches([]), false);
  assert.strictEqual(new SkipPathMatcher([]).matches(['a']), false);
  assert.strictEqual(new SkipPathMatcher([]).isEmpty, true);
  assert.ok(Object.isFrozen(m) && Object.isFrozen(m.patterns[0]));
  assert.throws(function () { m.matches('a'); }, TypeError);
});

test('SkipPathMatcher: input array is copied', function () {
  var pointers = ['/a'];
  var m = new SkipPathMatcher(pointers);
  pointers.push('/b');
  assert.strictEqual(m.matches(['b']), false);
  assert.deepStrictEqual(m.pointers.slice(), ['/a']);
});
