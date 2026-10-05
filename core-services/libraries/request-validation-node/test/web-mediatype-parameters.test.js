'use strict';

// Content-Type parsing against Spring 5.2.12 MediaType.parseMediaType and the parameter map against Tomcat 9.0.41
// (both from a Java-produced fixture), plus the qs-view extras (the text Express hands the application).

var assert = require('assert');
var path = require('path');
var h = require('./harness');
var test = h.test;
var mediaType = require(path.join(h.ROOT, 'lib', 'web', 'mediaType'));
var parameters = require(path.join(h.ROOT, 'lib', 'web', 'parameters'));

var fixture = h.readJsonFixture('spring-tomcat-web.json');

test('media types: parse, type, subtype, charset and isJson equal Spring for every fixture value', function () {
  var compared = 0;
  fixture.mediaTypes.forEach(function (c) {
    var node = mediaType.parse(c.input);
    var label = JSON.stringify(c.input);
    if (c.spring === 'NULL') {
      assert.strictEqual(node, null, label);
    } else if (c.spring === 'ERR') {
      // Spring also rejects a charset name Java does not know; the Node parser accepts it and the adapter reports
      // the charset instead (divergence D7). The fixture holds one such value: charset=''.
      if (c.input === 'application/json; charset=\'\'') {
        assert.ok(node !== null && node.charset === '\'\'' && !mediaType.isUtf8Charset(node.charset), label);
      } else {
        assert.strictEqual(node, null, label);
      }
    } else {
      assert.ok(node !== null, label);
      assert.strictEqual(node.type, c.type, label);
      assert.strictEqual(node.subtype, c.subtype, label);
      assert.strictEqual(node.charset === undefined ? null : node.charset, c.charset, label);
      assert.strictEqual(mediaType.isJson(node), c.isJson, label);
    }
    compared++;
  });
  assert.ok(compared >= 50);
});

test('media types: UTF-8 charset names (Java aliases), form type and type-is hasBody', function () {
  ['utf-8', 'UTF-8', 'utf8', 'UTF8', 'unicode-1-1-utf-8', '"utf-8"', '\'UTF-8\''].forEach(function (c) {
    assert.ok(mediaType.isUtf8Charset(c), c);
  });
  ['utf-16', 'ISO-8859-1', 'us-ascii', 'foo', '', '"utf-8', 'utf_8'].forEach(function (c) {
    assert.ok(!mediaType.isUtf8Charset(c), c);
  });
  assert.ok(mediaType.isUtf8Encoding('utf-8') && mediaType.isUtf8Encoding('UTF8') && !mediaType.isUtf8Encoding('utf-16')
      && !mediaType.isUtf8Encoding(undefined));
  assert.ok(mediaType.isForm(mediaType.parse('APPLICATION/X-WWW-FORM-URLENCODED; charset=utf-8')));
  assert.ok(!mediaType.isForm(mediaType.parse('application/x-www-form-urlencoded; x=<b>')));
  assert.ok(mediaType.hasBody({ headers: { 'content-length': '0' } }));
  assert.ok(mediaType.hasBody({ headers: { 'transfer-encoding': 'chunked' } }));
  assert.ok(!mediaType.hasBody({ headers: {} }));
});

test('parameter map: names, values, order and grouping equal Tomcat for every fixture request', function () {
  fixture.parameters.forEach(function (c) {
    var map = parameters.parameterMap(c.query, c.form === null ? null : Buffer.from(c.form, 'latin1'), 'utf-8');
    assert.deepStrictEqual(map.entries, c.tomcat, JSON.stringify([c.query, c.form]));
  });
  assert.ok(fixture.parameters.length >= 150);
});

test('parameter map: Tomcat rules (split on & only, first =, empty names and invalid escapes drop the pair)', function () {
  function entries(q, f) {
    return parameters.parameterMap(q, f === undefined ? null : Buffer.from(f, 'latin1'), 'utf-8').entries;
  }
  assert.deepStrictEqual(entries('q=1;onclick=2'), [['q', ['1;onclick=2']]]);
  assert.deepStrictEqual(entries('%3Cb'), [['<b', ['']]]);
  assert.deepStrictEqual(entries('=%3Cb'), []);
  assert.deepStrictEqual(entries('q=%3Cb%zz'), []);
  assert.deepStrictEqual(entries('q=%3Cb%'), []);
  assert.deepStrictEqual(entries('a=1&b=%3Cx', 'a=' + 'y'.repeat(3)), [['a', ['1', 'yyy']], ['b', ['<x']]]);
  assert.deepStrictEqual(entries('b=%3Cx&a=1', 'a=yyy'), [['b', ['<x']], ['a', ['1', 'yyy']]]);
  assert.deepStrictEqual(entries('x=a+b%2Bc'), [['x', ['a b+c']]]);
  assert.deepStrictEqual(entries('q=%ED%A0%80'), [['q', ['\uFFFD']]], 'Java lossy UTF-8: one replacement for an encoded surrogate');
});

test('extras: the strings of the application views (qs, querystring) that the Tomcat map does not hold', function () {
  function map(q, f, enc) {
    return parameters.parameterMap(q, f === undefined ? null : Buffer.from(f, 'latin1'), enc || 'utf-8');
  }
  function extras(q, f, enc) {
    return map(q, f, enc).extras;
  }
  assert.deepStrictEqual(extras('a=1&b=%3Cx&c=x+y&d=a%2Bb'), [], 'no extras on well-formed requests');
  assert.deepStrictEqual(extras('q=<script>%zz'), [{ name: 'q', value: 'q' }, { name: 'q', value: '<script>%zz' }]);
  assert.deepStrictEqual(extras('q=%3Cb%zz'), [{ name: 'q', value: 'q' }, { name: 'q', value: '%3Cb%zz' },
    { name: 'q', value: '<b%zz' }], 'querystring decodes a malformed value partially');
  assert.deepStrictEqual(extras('q=%FF'), [{ name: 'q', value: '%FF' }],
      'invalid UTF-8: Tomcat and querystring both give one U+FFFD; qs keeps the text');
  assert.deepStrictEqual(extras('link[x=y]=javascript:alert(1)'), [{ name: 'link[x=y]', value: 'link[x=y]' },
    { name: 'link[x=y]', value: 'javascript:alert(1)' }], 'qs splits at "]="');
  assert.deepStrictEqual(extras('a%5Bx=y%5D=javascript:1'), [{ name: 'a[x=y]', value: 'a[x=y]' },
    { name: 'a[x=y]', value: 'javascript:1' }], 'qs 6.10+ reads %5B and %5D as brackets before it splits');
  assert.deepStrictEqual(extras(null, 'a=caf%E9', 'iso-8859-1'), [{ name: 'a', value: 'caf%E9' },
    { name: 'a', value: 'café' }], 'latin-1 form: qs without and with the charset (querystring gives the map value)');
  assert.deepStrictEqual(extras(null, '\u00ef\u00bb\u00bfjs=1'), [{ name: 'js', value: 'js' }], 'body-parser drops a UTF-8 BOM');
  var empty = map('=%3Cb&a=1', '=%3Cs');
  assert.deepStrictEqual(empty.extras, [], 'an empty name is not an extra');
  assert.deepStrictEqual(empty.queryEmptyNames, ['<b']);
  assert.deepStrictEqual(empty.formEmptyNames, ['<s']);
  assert.deepStrictEqual(parameters.rawQuery('/a?b=1?c'), 'b=1?c');
  assert.strictEqual(parameters.rawQuery('/a'), null);
});

test('parameter map: Tomcat keeps 10,000 parameters and no body over 2 MiB; the rest is left for after the parser', function () {
  var pairs = [];
  for (var i = 0; i < 10005; i++) {
    pairs.push('p' + i + '=' + (i === 10002 ? '%3Cb%3E' : '1'));
  }
  var m = parameters.parameterMap('q=1', Buffer.from(pairs.join('&'), 'latin1'), 'utf-8');
  var count = 0;
  m.entries.forEach(function (e) {
    count += e[1].length;
  });
  assert.strictEqual(count, 10000, 'query and body together');
  assert.ok(m.deferred !== null && m.deferred.start > 0);
  var deferred = [];
  parameters.deferredExtras(m.deferred, null, function (name, value) {
    deferred.push(name + '=' + value);
  }, function () {});
  assert.deepStrictEqual(deferred, ['p9999=p9999', 'p10000=p10000', 'p10001=p10001', 'p10002=p10002', 'p10002=<b>',
    'p10003=p10003', 'p10004=p10004'], 'names and values the map does not hold ("1" is in the map)');
  var big = Buffer.from('a=%3Cb%3E&f=' + 'x'.repeat(parameters.MAX_FORM_BYTES), 'latin1');
  var m2 = parameters.parameterMap('q=1', big, 'utf-8');
  assert.deepStrictEqual(m2.entries, [['q', ['1']]], 'a form body over 2 MiB is not in the map');
  assert.strictEqual(m2.deferred.start, 0);
  var stopped = 0;
  assert.throws(function () {
    var ticks = 0;
    parameters.parameterMap(null, Buffer.from(new Array(20001).join('a&'), 'latin1'), 'utf-8', { tick: function () {
      ticks++;
      if (ticks === 3) {
        stopped = ticks;
        throw new Error('deadline');
      }
    } });
  }, /deadline/);
  assert.strictEqual(stopped, 3, 'the deadline is checked while the map is built');
});
