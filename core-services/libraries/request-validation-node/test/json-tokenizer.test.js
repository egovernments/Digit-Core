'use strict';

// Tokenizer behaviour that decides which call fails: the value pre-parsed with a name, lazy strings, the end of the
// last successful read, the identifier check after literals, root numbers, and Jackson's \u digit lookup.

var assert = require('assert');
var h = require('./harness');
var test = h.test;

var Tokenizer = h.lib('jackson/Tokenizer');
var utf8 = h.lib('jdk/utf8');

var LIMITS = { maxNestingDepth: 64, maxStringLength: 1000000, maxNumberLength: 1000 };

function tokenizer(text, options) {
  return new Tokenizer({ text: text, availEnd: text.length, failAtEnd: false }, options || LIMITS);
}

var NAMES = {};
['START_OBJECT', 'END_OBJECT', 'START_ARRAY', 'END_ARRAY', 'FIELD_NAME', 'VALUE_STRING', 'VALUE_NUMBER_INT',
  'VALUE_NUMBER_FLOAT', 'VALUE_TRUE', 'VALUE_FALSE', 'VALUE_NULL'].forEach(function (n) {
  NAMES[Tokenizer[n]] = n;
});

/** Token names until the end, or until a failure ("!JsonParseError" / "!StreamConstraintsError"). */
function stream(t) {
  var out = [];
  for (;;) {
    var token;
    try {
      token = t.nextToken();
    } catch (e) {
      out.push('!' + e.name);
      return out;
    }
    if (token === null) {
      return out;
    }
    var label = NAMES[token];
    if (token === Tokenizer.FIELD_NAME) {
      label += ':' + t.getCurrentName();
    } else if (token === Tokenizer.VALUE_STRING) {
      try {
        label += ':' + t.getText();
      } catch (e) {
        out.push(label + ' !' + e.name);
        return out;
      }
    } else if (token === Tokenizer.VALUE_NUMBER_INT || token === Tokenizer.VALUE_NUMBER_FLOAT) {
      label += ':' + t.getTextLength();
    }
    out.push(label);
  }
}

test('token stream of a small document', function () {
  assert.deepStrictEqual(stream(tokenizer('{"a":[1,-2.5e3,"x",true,false,null,{}]}')), [
    'START_OBJECT', 'FIELD_NAME:a', 'START_ARRAY', 'VALUE_NUMBER_INT:1', 'VALUE_NUMBER_FLOAT:6', 'VALUE_STRING:x',
    'VALUE_TRUE', 'VALUE_FALSE', 'VALUE_NULL', 'START_OBJECT', 'END_OBJECT', 'END_ARRAY', 'END_OBJECT']);
  assert.deepStrictEqual(stream(tokenizer(' \t\r\n"root" ')), ['VALUE_STRING:root']);
  assert.deepStrictEqual(stream(tokenizer('{} {}')), ['START_OBJECT', 'END_OBJECT', 'START_OBJECT', 'END_OBJECT']);
});

test('the call that returns a name also pre-parses its value: literal and number errors surface there', function () {
  assert.deepStrictEqual(stream(tokenizer('{"a":tru}')), ['START_OBJECT', '!JsonParseError']);
  assert.deepStrictEqual(stream(tokenizer('{"a":01}')), ['START_OBJECT', '!JsonParseError']);
  assert.deepStrictEqual(stream(tokenizer('{"a":}')), ['START_OBJECT', '!JsonParseError']);
  assert.deepStrictEqual(stream(tokenizer('{"a":1x}')), ['START_OBJECT', 'FIELD_NAME:a', 'VALUE_NUMBER_INT:1', '!JsonParseError']);
  assert.deepStrictEqual(stream(tokenizer('{"a":12345}', { maxNestingDepth: 64, maxStringLength: 100, maxNumberLength: 4 })),
      ['START_OBJECT', '!StreamConstraintsError']);
  // containers after a name are only recognised; their depth is checked by the next call
  assert.deepStrictEqual(stream(tokenizer('{"a":[]}', { maxNestingDepth: 1, maxStringLength: 100, maxNumberLength: 4 })),
      ['START_OBJECT', 'FIELD_NAME:a', '!StreamConstraintsError']);
});

test('string values are scanned lazily: their errors surface in getText()', function () {
  assert.deepStrictEqual(stream(tokenizer('{"a":"x\\qy"}')), ['START_OBJECT', 'FIELD_NAME:a', 'VALUE_STRING !JsonParseError']);
  assert.deepStrictEqual(stream(tokenizer('["a\tb"]')), ['START_ARRAY', 'VALUE_STRING !JsonParseError']);
  assert.deepStrictEqual(stream(tokenizer('["abc"]', { maxNestingDepth: 64, maxStringLength: 2, maxNumberLength: 9 })),
      ['START_ARRAY', 'VALUE_STRING !StreamConstraintsError']);
  // a name is scanned eagerly
  assert.deepStrictEqual(stream(tokenizer('{"a\\q":1}')), ['START_OBJECT', '!JsonParseError']);
});

test('a failed read surfaces exactly when the parser needs a unit past the last successful read', function () {
  function view(text, failAtEnd) {
    return new Tokenizer({ text: text, availEnd: text.length, failAtEnd: failAtEnd }, LIMITS);
  }
  // the number needs the unit after "2" to know it ended
  assert.deepStrictEqual(stream(view('[1,2', true)), ['START_ARRAY', 'VALUE_NUMBER_INT:1', '!JsonParseError']);
  assert.deepStrictEqual(stream(view('[1,2', false)), ['START_ARRAY', 'VALUE_NUMBER_INT:1', 'VALUE_NUMBER_INT:1', '!JsonParseError']);
  // a literal looks at the next unit too
  assert.deepStrictEqual(stream(view('[true', true)), ['START_ARRAY', '!JsonParseError']);
  // a string that ends before the failing read is complete
  assert.deepStrictEqual(stream(view('["<b>"', true)), ['START_ARRAY', 'VALUE_STRING:<b>', '!JsonParseError']);
  // the same through readerView: 0xFF in the second read; the first read (4,000 units) ends with the comma
  var body = Buffer.concat([Buffer.from('["' + 'a'.repeat(3996) + '",1'), Buffer.from([0xFF])]);
  var v = utf8.readerView(body, 0);
  assert.strictEqual(v.availEnd, 4000);
  assert.strictEqual(v.failAtEnd, true);
  assert.deepStrictEqual(stream(new Tokenizer(v, LIMITS)).length, 3);
});

test('after true/false/null a Java 8 identifier part fails the literal; other units leave it to the next token', function () {
  ['x', '1', '_', '\u20AC', '\u00E9', '\u00AD', '\u0300'].forEach(function (unit) {
    assert.deepStrictEqual(stream(tokenizer('[true' + unit + ']')), ['START_ARRAY', '!JsonParseError'], JSON.stringify(unit));
  });
  // units below '0' are never checked ('$' included); ']' and '}' pass; the rest must not be identifier parts
  ['<', ' ', ']', '$', '\u00B7', '\u2028', '\uD83D\uDE00', '#'].forEach(function (unit) {
    var s = stream(tokenizer('[null' + unit + ']'));
    assert.strictEqual(s[1], 'VALUE_NULL', JSON.stringify(unit));
  });
});

test('a root number must be followed by white space or the end of input', function () {
  assert.deepStrictEqual(stream(tokenizer('12')), ['VALUE_NUMBER_INT:2']);
  assert.deepStrictEqual(stream(tokenizer('12 ')), ['VALUE_NUMBER_INT:2']);
  assert.deepStrictEqual(stream(tokenizer('-0.5\r\n')), ['VALUE_NUMBER_FLOAT:4']);
  assert.deepStrictEqual(stream(tokenizer('12]')), ['!JsonParseError']);
  assert.deepStrictEqual(stream(tokenizer('{} 5x')), ['START_OBJECT', 'END_OBJECT', '!JsonParseError']);
});

test('numbers: JSON grammar with Jackson defaults', function () {
  ['0', '-0', '0.5', '-0.0e-0', '1E+9', '123456789012345678901234567890'].forEach(function (n) {
    assert.ok(stream(tokenizer('[' + n + ']'))[1].indexOf('VALUE_NUMBER') === 0, n);
  });
  ['01', '-01', '1.', '.5', '+1', '1e', '1e+', '-', '--1', 'NaN', 'Infinity', '-Infinity', '0x10'].forEach(function (n) {
    var s = stream(tokenizer('[' + n + ']'));
    assert.strictEqual(s[s.length - 1], '!JsonParseError', n);
  });
});

test('white space between tokens is only space, TAB, LF and CR', function () {
  ['\u000b', '\u000c', '\u00A0', '\uFEFF', '\u2028', '\u0000'].forEach(function (ws) {
    var s = stream(tokenizer('[1,' + ws + '2]'));
    assert.strictEqual(s[s.length - 1], '!JsonParseError', JSON.stringify(ws));
  });
  assert.deepStrictEqual(stream(tokenizer('[1,\r\n\t 2]')), ['START_ARRAY', 'VALUE_NUMBER_INT:1', 'VALUE_NUMBER_INT:1', 'END_ARRAY']);
});

test('\\u escapes use the low byte of each unit as the hex digit, as Jackson does', function () {
  var s = stream(tokenizer('["\\u\u0130\u01303C", "\\u00\u0133\u0143", "\\u00\u0133c"]'));
  assert.deepStrictEqual(s, ['START_ARRAY', 'VALUE_STRING:<', 'VALUE_STRING:<', 'VALUE_STRING:<', 'END_ARRAY']);
  assert.deepStrictEqual(stream(tokenizer('["\\u\uFF10\uFF10\uFF13\uFF23"]')), ['START_ARRAY', 'VALUE_STRING !JsonParseError']);
  assert.deepStrictEqual(stream(tokenizer('["\\uD800", "\\u0000"]')),
      ['START_ARRAY', 'VALUE_STRING:\uD800', 'VALUE_STRING:\u0000', 'END_ARRAY']);
});

test('nesting depth is checked when the container token is created', function () {
  var opts = { maxNestingDepth: 2, maxStringLength: 100, maxNumberLength: 9 };
  assert.deepStrictEqual(stream(tokenizer('[[]]', opts)), ['START_ARRAY', 'START_ARRAY', 'END_ARRAY', 'END_ARRAY']);
  assert.deepStrictEqual(stream(tokenizer('[[[]]]', opts)), ['START_ARRAY', 'START_ARRAY', '!StreamConstraintsError']);
});

test('end of input inside an open container, after a comma, or in a name fails; a mismatched close fails', function () {
  ['[', '[1,', '{"a"', '{"a":', '{"a', '["a', '{"a":1', ']', '[}', '{]', '{"a":1,}', '[1,]', '[,1]', '{,}'].forEach(function (doc) {
    var s = stream(tokenizer(doc));
    assert.ok(/!JsonParseError$/.test(s[s.length - 1]), doc + ': ' + s.join(','));
  });
});
