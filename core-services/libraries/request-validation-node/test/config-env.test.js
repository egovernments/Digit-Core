'use strict';

// Spring Boot 2.2 conversion rules for settings given as text: placeholders, blank values, booleans,
// integers (decimal and hexadecimal), Java doubles, lenient enum names and lists.

var assert = require('assert');
var path = require('path');
var h = require('./harness');
var test = h.test;
var conv = require(path.join(h.ROOT, 'lib', 'config', 'env'));

function lookupOf(env) {
  return conv.environmentLookup(env);
}

test('placeholders: ${NAME}, ${NAME:default}, recursion and nested keys', function () {
  var look = lookupOf({ A: 'x', B: '${A}y', C: 'A', EMPTY: '', SP: ' ' });
  assert.strictEqual(conv.resolvePlaceholders('${A}', look, false), 'x');
  assert.strictEqual(conv.resolvePlaceholders('pre-${B}-post', look, false), 'pre-xy-post');
  assert.strictEqual(conv.resolvePlaceholders('${MISSING:dflt}', look, false), 'dflt');
  assert.strictEqual(conv.resolvePlaceholders('${MISSING:}', look, false), '');
  assert.strictEqual(conv.resolvePlaceholders('${MISSING:${A}}', look, false), 'x');
  assert.strictEqual(conv.resolvePlaceholders('${${C}}', look, false), 'x');
  assert.strictEqual(conv.resolvePlaceholders('${EMPTY:fallback}', look, false), '', 'a set but empty variable wins');
  assert.strictEqual(conv.resolvePlaceholders('${A}${A}', look, false), 'xx');
  assert.strictEqual(conv.resolvePlaceholders('no placeholder', look, false), 'no placeholder');
  assert.strictEqual(conv.resolvePlaceholders('${unterminated', look, false), '${unterminated');
});

test('placeholders: unresolvable stays literal (binder) or throws (strict); circular references throw', function () {
  var look = lookupOf({ LOOP: '${LOOP}', P: '${Q}', Q: '${P}' });
  assert.strictEqual(conv.resolvePlaceholders('${NOPE}', look, false), '${NOPE}');
  assert.strictEqual(conv.resolvePlaceholders('a${NOPE}b${NOPE}', look, false), 'a${NOPE}b${NOPE}');
  assert.throws(function () {
    conv.resolvePlaceholders('${NOPE}', look, true);
  }, /Could not resolve placeholder 'NOPE'/);
  assert.throws(function () {
    conv.resolvePlaceholders('${LOOP}', look, false);
  }, /Circular placeholder reference 'LOOP'/);
  assert.throws(function () {
    conv.resolvePlaceholders('${P}', look, false);
  }, /Circular placeholder reference/);
});

test('placeholders: environment names are matched as SystemEnvironmentPropertySource matches them', function () {
  var look = lookupOf({ RV_MODE: 'ENFORCE', 'rv_depth': '7', 'SOME_KEY': 'k' });
  assert.strictEqual(conv.resolvePlaceholders('${rv.mode}', look, false), 'ENFORCE');
  assert.strictEqual(conv.resolvePlaceholders('${rv-mode}', look, false), 'ENFORCE');
  assert.strictEqual(conv.resolvePlaceholders('${rv.depth}', look, false), '7');
  assert.strictEqual(conv.resolvePlaceholders('${some.key}', look, false), 'k');
  assert.strictEqual(conv.resolvePlaceholders('${other}', look, false), '${other}');
});

test('blank values: a blank scalar is "not set"; an empty list value is an empty list; a whitespace-only list is not set', function () {
  var look = lookupOf({ RV_BLANK: ' ', RV_EMPTY: '' });
  assert.strictEqual(conv.settingText('', look, false), undefined);
  assert.strictEqual(conv.settingText('  \t', look, false), undefined);
  assert.strictEqual(conv.settingText('${RV_BLANK}', look, false), undefined);
  assert.strictEqual(conv.settingText('${RV_EMPTY:}', look, false), undefined);
  assert.strictEqual(conv.settingText('', look, true), '');
  assert.strictEqual(conv.settingText('${RV_EMPTY}', look, true), '');
  assert.strictEqual(conv.settingText(' ', look, true), undefined);
  assert.strictEqual(conv.settingText('${MISSING}', look, false), '${MISSING}', 'an unresolvable placeholder is not blank');
  assert.strictEqual(conv.settingText(' x ', look, false), ' x ');
});

test('booleans: true|on|yes|1 and false|off|no|0, trimmed, any case; anything else fails', function () {
  ['true', 'TRUE', ' on ', 'Yes', '1', '\ttrue\n'].forEach(function (v) {
    assert.strictEqual(conv.toBoolean(v), true, JSON.stringify(v));
  });
  ['false', 'Off', 'NO', '0', ' false '].forEach(function (v) {
    assert.strictEqual(conv.toBoolean(v), false, JSON.stringify(v));
  });
  assert.strictEqual(conv.toBoolean(''), null);
  ['y', 'n', 'maybe', 'tru', '2', 'true!', ' true'].forEach(function (v) {
    assert.throws(function () {
      conv.toBoolean(v);
    }, RangeError, JSON.stringify(v));
  });
});

test('integers: decimal with sign, hexadecimal with 0x, 0X or #, all whitespace removed, Java ranges', function () {
  var cases = [['0', 0], ['42', 42], ['+5', 5], ['-7', -7], ['010', 10], ['0x10', 16], ['0X1F', 31], ['#20', 32],
    ['-0x10', -16], ['-#10', -16], ['1 0', 10], [' 7 ', 7], ['\t64\n', 64], ['2147483647', 2147483647],
    ['-2147483648', -2147483648], ['0x7fffffff', 2147483647],
    // Character.digit: any Unicode decimal digit, fullwidth Latin letters as hexadecimal digits (Spring Boot 2.2
    // starts with these values)
    ['\u0661', 1], ['\u0663', 3], ['\uFF13', 3], ['0x\u0663', 3], ['#\uFF13', 3], ['\u0661\u0662\u0663', 123],
    ['\uFF11\uFF12\uFF13', 123], ['0x\uFF21', 10], ['-\u0663', -3], ['\u09E9', 3]];
  cases.forEach(function (c) {
    assert.strictEqual(conv.toInteger(c[0], 32), c[1], JSON.stringify(c[0]));
  });
  ['2147483648', '0x80000000', '1e3', '1.5', 'abc', '0x', '#', '+', '-', '+0x10', '0x-1', '0x+1', '1_000', '0b1',
    '\uD835\uDFD1', '\uFF21', '\u00B2', '\uFF0D1', '\u2163']
      .forEach(function (v) {
        assert.throws(function () {
          conv.toInteger(v, 32);
        }, RangeError, JSON.stringify(v));
      });
  assert.strictEqual(conv.toInteger('2147483648', 64), 2147483648);
  assert.strictEqual(conv.toInteger('0x80000000', 64), 2147483648);
  assert.strictEqual(conv.toInteger('', 32), null);
});

test('doubles: Java Double.valueOf syntax after whitespace removal', function () {
  var cases = [['0', 0], ['1', 1], ['0.5', 0.5], ['.5', 0.5], ['5.', 5], ['1e-1', 0.1], ['1E0', 1], ['0.25d', 0.25],
    ['0.5f', 0.5], ['0x0.8p0', 0.5], ['0x1p-1', 0.5], ['-0', -0], ['+1', 1], [' 0 . 5 ', 0.5], ['1e+0', 1]];
  cases.forEach(function (c) {
    assert.ok(Object.is(conv.toDouble(c[0]), c[1]), JSON.stringify(c[0]) + ' -> ' + conv.toDouble(c[0]));
  });
  assert.ok(isNaN(conv.toDouble('NaN')));
  assert.strictEqual(conv.toDouble('Infinity'), Infinity);
  assert.strictEqual(conv.toDouble('-Infinity'), -Infinity);
  ['abc', '0x10', '1,5', 'e1', '.', '1e', '1e+', 'nan', 'infinity', '0x1.8', '1.0dd', '--1'].forEach(function (v) {
    assert.throws(function () {
      conv.toDouble(v);
    }, RangeError, JSON.stringify(v));
  });
  assert.strictEqual(conv.toDouble(''), null);
});

test('enums: exact name, else case and separators ignored (Spring Boot lenient enum conversion)', function () {
  var modes = ['DEFAULT', 'REPORT', 'ENFORCE'];
  [['ENFORCE', 'ENFORCE'], ['enforce', 'ENFORCE'], ['En-Force', 'ENFORCE'], ['en_force', 'ENFORCE'], [' report ', 'REPORT'],
    ['re.port', 'REPORT'], ['en force', 'ENFORCE'], ['default', 'DEFAULT']].forEach(function (c) {
    assert.strictEqual(conv.toEnum(c[0], modes), c[1], JSON.stringify(c[0]));
  });
  assert.strictEqual(conv.toEnum('a.l.l', ['ANNOTATED', 'ALL']), 'ALL');
  ['x', 'enforced', 'Énforce', 'ALL'].forEach(function (v) {
    assert.throws(function () {
      conv.toEnum(v, modes);
    }, RangeError, JSON.stringify(v));
  });
  assert.strictEqual(conv.toEnum('', modes), null);
});

test('lists: comma-separated, each element trimmed, an empty value is an empty list', function () {
  assert.deepStrictEqual(conv.splitList('a'), ['a']);
  assert.deepStrictEqual(conv.splitList(' a , b '), ['a', 'b']);
  assert.deepStrictEqual(conv.splitList('a,,b'), ['a', '', 'b']);
  assert.deepStrictEqual(conv.splitList(','), ['', '']);
  assert.deepStrictEqual(conv.splitList(''), []);
});
