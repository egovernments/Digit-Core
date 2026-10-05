'use strict';

// The request parameters Java inspects, rebuilt from raw bytes as Tomcat 9 builds getParameterMap(): the query
// string first, then a urlencoded body, split on '&' only, name and value decoded with '+' as space and %XX escapes,
// then Java's lossy UTF-8 decoding; a pair with an invalid escape or an empty name is dropped; values are grouped
// under the first appearance of their name. Tomcat keeps at most 10,000 parameters (query and body together) and
// does not parse a urlencoded body larger than 2 MiB (Spring Boot's default form size).
//
// Express hands the application other views of the same pairs: qs (Express 4's query parser, body-parser's
// extended parser, every body-parser 2 urlencoded parser) splits a pair at ']=' when there is one and keeps the text
// of a malformed escape; Node's querystring (Express 5's query parser, body-parser 1 with extended: false) decodes a
// malformed value partially and keeps a pair with an empty name. Every string of those views that is not a string of
// the Tomcat map is returned as an extra, inspected after the map. Pairs Tomcat does not keep (beyond its limits) are
// left for later: they are inspected only once the body parser has accepted the body.

var utf8 = require('../core/jdk/utf8');

var MAX_PARAMETER_COUNT = 10000;
var MAX_FORM_BYTES = 2097152;
var TICK_EVERY = 256;
var LONG_PIECE = 4096;

function hexValue(b) {
  if (b >= 0x30 && b <= 0x39) {
    return b - 0x30;
  }
  if (b >= 0x61 && b <= 0x66) {
    return b - 0x61 + 10;
  }
  if (b >= 0x41 && b <= 0x46) {
    return b - 0x41 + 10;
  }
  return -1;
}

/** Tomcat UDecoder.convert(bytes, query = true): '+' to space, %XX to a byte; null for an invalid escape. */
function tomcatDecode(bytes, start, end) {
  var simple = true;
  for (var i = start; i < end; i++) {
    var c = bytes[i];
    if (c === 0x25 || c === 0x2B || c >= 0x80) {
      simple = false;
      break;
    }
  }
  if (simple) {
    return bytes.toString('latin1', start, end);
  }
  var out = Buffer.alloc(end - start);
  var n = 0;
  for (var j = start; j < end; j++) {
    var b = bytes[j];
    if (b === 0x2B) {
      out[n++] = 0x20;
    } else if (b !== 0x25) {
      out[n++] = b;
    } else {
      if (j + 2 >= end) {
        return null;
      }
      var h1 = hexValue(bytes[j + 1]);
      var h2 = hexValue(bytes[j + 2]);
      if (h1 < 0 || h2 < 0) {
        return null;
      }
      out[n++] = (h1 << 4) | h2;
      j += 2;
    }
  }
  return utf8.decodeLossy(out, 0, n);
}

function hasEscapeOrPlus(text) {
  for (var i = 0; i < text.length; i++) {
    var c = text.charCodeAt(i);
    if (c === 0x25 || c === 0x2B) {
      return true;
    }
  }
  return false;
}

// '+' as a space, as qs and querystring read it. This is the one regular expression applied to request text; it
// matches single characters only.
function plusToSpace(text) {
  return text.replace(/\+/g, ' ');
}

// %XX decoded as one Latin-1 character each, everything else kept (qs for charset iso-8859-1).
function latin1PercentDecode(text) {
  var out = '';
  var from = 0;
  for (var i = 0; i + 2 < text.length; i++) {
    if (text.charCodeAt(i) === 0x25) {
      var h1 = hexValue(text.charCodeAt(i + 1));
      var h2 = hexValue(text.charCodeAt(i + 2));
      if (h1 >= 0 && h2 >= 0) {
        out += text.slice(from, i) + String.fromCharCode((h1 << 4) | h2);
        i += 2;
        from = i + 1;
      }
    }
  }
  return from === 0 ? text : out + text.slice(from);
}

/** The text qs gives the application for one name or value. */
function qsDecode(text, latin1) {
  if (!hasEscapeOrPlus(text)) {
    return text;
  }
  var withSpaces = plusToSpace(text);
  if (latin1) {
    return latin1PercentDecode(withSpaces);
  }
  try {
    return decodeURIComponent(withSpaces);
  } catch (e) {
    return withSpaces;
  }
}

/** Node's querystring.unescapeBuffer(text).toString(): valid %XX escapes decoded, the rest kept, read as UTF-8. */
function partialDecode(text) {
  var out = Buffer.alloc(text.length);
  var n = 0;
  for (var i = 0; i < text.length; i++) {
    var c = text.charCodeAt(i);
    if (c === 0x25 && i < text.length - 2) {
      var h1 = hexValue(text.charCodeAt(i + 1));
      var h2 = h1 < 0 ? -1 : hexValue(text.charCodeAt(i + 2));
      if (h2 >= 0) {
        out[n++] = (h1 << 4) | h2;
        i += 2;
        continue;
      }
    }
    out[n++] = c & 0xFF;
  }
  return out.toString('utf8', 0, n);
}

/** The text Node's querystring gives the application for one name or value. */
function querystringDecode(text) {
  var plus = false;
  var encoded = false;
  for (var i = 0; i < text.length; i++) {
    var c = text.charCodeAt(i);
    if (c === 0x2B) {
      plus = true;
    } else if (c === 0x25 && i + 2 < text.length && hexValue(text.charCodeAt(i + 1)) >= 0
        && hexValue(text.charCodeAt(i + 2)) >= 0) {
      encoded = true;
    }
  }
  var withSpaces = plus ? plusToSpace(text) : text;
  if (!encoded) {
    return withSpaces;
  }
  try {
    return decodeURIComponent(withSpaces);
  } catch (e) {
    return partialDecode(withSpaces);
  }
}

// qs replaces %5B and %5D (any case) by brackets before it splits the pairs (qs 6.10 and later).
function bracketsDecoded(text) {
  var out = '';
  var from = 0;
  for (var i = 0; i + 2 < text.length; i++) {
    if (text.charCodeAt(i) === 0x25 && text.charCodeAt(i + 1) === 0x35) {
      var c = text.charCodeAt(i + 2) | 0x20;
      if (c === 0x62 || c === 0x64) {
        out += text.slice(from, i) + (c === 0x62 ? '[' : ']');
        i += 2;
        from = i + 1;
      }
    }
  }
  return from === 0 ? text : out + text.slice(from);
}

// The position of the '=' that ends a qs key: the one in ']=' when there is one, else the first '='.
function qsSplit(text) {
  for (var i = 0; i + 1 < text.length; i++) {
    if (text.charCodeAt(i) === 0x5D && text.charCodeAt(i + 1) === 0x3D) {
      return i + 1;
    }
  }
  return text.indexOf('=');
}

function firstEquals(text) {
  return text.indexOf('=');
}

/**
 * The application views of one pair: [name, value] for qs (with and without the bracket rewrite, and Latin-1
 * decoding for an iso-8859-1 form) and for Node's querystring. A view whose name is empty is reported through
 * `onEmptyName(value)` instead: only querystring keeps such a pair.
 */
function appViews(text, latin1, out, onEmptyName) {
  var cleaned = bracketsDecoded(text);
  var qsTexts = cleaned === text ? [text] : [cleaned, text];
  for (var k = 0; k < qsTexts.length; k++) {
    var t = qsTexts[k];
    var eq = qsSplit(t);
    var rawName = eq === -1 ? t : t.slice(0, eq);
    var rawValue = eq === -1 ? '' : t.slice(eq + 1);
    var decodings = latin1 ? [false, true] : [false];
    for (var d = 0; d < decodings.length; d++) {
      var name = qsDecode(rawName, decodings[d]);
      if (name.length !== 0) {
        out.push(name, qsDecode(rawValue, decodings[d]));
      }
    }
  }
  var eqq = firstEquals(text);
  var qName = querystringDecode(eqq === -1 ? text : text.slice(0, eqq));
  var qValue = eqq === -1 ? '' : querystringDecode(text.slice(eqq + 1));
  if (qName.length === 0) {
    onEmptyName(qValue);
  } else {
    out.push(qName, qValue);
  }
}

/** The raw query string of a request target: everything after the first '?', or null. */
function rawQuery(target) {
  if (typeof target !== 'string') {
    return null;
  }
  var q = target.indexOf('?');
  return q === -1 ? null : target.slice(q + 1);
}

/** Whether every view reads the pair as its raw text split at the first '=': no '%', '+' or ']'. */
function isPlainPair(text) {
  for (var i = 0; i < text.length; i++) {
    var c = text.charCodeAt(i);
    if (c === 0x25 || c === 0x2B || c === 0x5D) {
      return false;
    }
  }
  return true;
}

/**
 * Collects the strings of the application views of one pair that are not strings of the Tomcat map, as
 * { name, value } extras (name: the view's parameter name, for the location).
 */
function addExtras(pieceText, latin1, known, extras, emptyNames) {
  if (isPlainPair(pieceText)) {
    var eq = pieceText.indexOf('=');
    var plainName = eq === -1 ? pieceText : pieceText.slice(0, eq);
    var plainValue = eq === -1 ? '' : pieceText.slice(eq + 1);
    if (plainName.length === 0) {
      emptyNames.push(plainValue);
      return;
    }
    if (!known.has(plainName)) {
      extras.push({ name: plainName, value: plainName });
    }
    if (plainValue !== plainName && !known.has(plainValue)) {
      extras.push({ name: plainName, value: plainValue });
    }
    return;
  }
  var views = [];
  appViews(pieceText, latin1, views, function (value) {
    emptyNames.push(value);
  });
  var seen = null;
  for (var i = 0; i < views.length; i += 2) {
    var name = views[i];
    var value = views[i + 1];
    for (var j = 0; j < 2; j++) {
      var s = j === 0 ? name : value;
      if (known.has(s) || (seen !== null && seen.indexOf(s) !== -1)) {
        continue;
      }
      if (seen === null) {
        seen = [];
      }
      seen.push(s);
      extras.push({ name: name, value: s });
    }
  }
}

/** Calls `onPiece(start, end)` for each non-empty '&'-separated piece of `bytes` in [from, length). */
function forEachPiece(bytes, from, tick, onPiece) {
  var length = bytes.length;
  var pos = from;
  var count = 0;
  while (pos < length) {
    var end = bytes.indexOf(0x26, pos);
    if (end === -1) {
      end = length;
    }
    var start = pos;
    pos = end + 1;
    if (end === start) {
      continue;
    }
    if (tick !== null && (++count % TICK_EVERY === 0 || end - start > LONG_PIECE)) {
      tick();
    }
    if (onPiece(start, end) === false) {
      return start;
    }
  }
  return -1;
}

/**
 * The parameter map and the extras for a request.
 * @param {string|null} query the raw query string (as Node delivers it: one character per byte)
 * @param {Buffer|null} form the urlencoded body bytes, or null
 * @param {string} [formEncoding] the body parser's charset for the form ('utf-8' or 'iso-8859-1')
 * @param {object} [options] appQuery: the query string the application's parser receives (default: query);
 *   tick: called every 256 pairs, may throw to stop
 * @returns {{ entries: Array, extras: Array, queryEmptyNames: Array, formEmptyNames: Array, deferred: object }}
 *   entries: [name, values[]] in map order; extras: { name, value }; *EmptyNames: values of pairs whose name is
 *   empty in Node's querystring view; deferred: null, or { form, start, latin1, known } for the body pairs Tomcat
 *   does not keep
 */
function parameterMap(query, form, formEncoding, options) {
  var opts = options || {};
  var tick = typeof opts.tick === 'function' ? opts.tick : null;
  var map = new Map();
  var known = new Set();
  var count = 0;
  var queryBytes = query !== null && query !== undefined && query.length > 0 ? Buffer.from(query, 'latin1') : null;

  function addPair(bytes, start, end) {
    var eq = -1;
    for (var i = start; i < end; i++) {
      if (bytes[i] === 0x3D) {
        eq = i;
        break;
      }
    }
    var nameEnd = eq === -1 ? end : eq;
    if (nameEnd === start) {
      return true; // empty name: dropped, not counted
    }
    var name = tomcatDecode(bytes, start, nameEnd);
    var value = eq === -1 ? '' : tomcatDecode(bytes, eq + 1, end);
    if (name === null || value === null) {
      return true; // invalid escape: dropped, not counted
    }
    if (count >= MAX_PARAMETER_COUNT) {
      return false;
    }
    count++;
    var values = map.get(name);
    if (values === undefined) {
      values = [];
      map.set(name, values);
    }
    values.push(value);
    known.add(name);
    known.add(value);
    return true;
  }

  if (queryBytes !== null) {
    forEachPiece(queryBytes, 0, tick, function (start, end) {
      return addPair(queryBytes, start, end);
    });
  }
  var latin1 = typeof formEncoding === 'string' && formEncoding.toLowerCase() === 'iso-8859-1';
  // body-parser decodes a UTF-8 body with iconv-lite, which drops a leading byte order mark; Java keeps it.
  var bomEnd = form && !latin1 && form.length >= 3 && form[0] === 0xEF && form[1] === 0xBB && form[2] === 0xBF ? 3 : 0;
  var deferredFrom = -1;
  var formUntil = 0;
  if (form && form.length > 0) {
    if (form.length > MAX_FORM_BYTES) {
      deferredFrom = 0;
    } else {
      deferredFrom = forEachPiece(form, 0, tick, function (start, end) {
        return addPair(form, start, end);
      });
    }
    formUntil = deferredFrom === -1 ? form.length : deferredFrom;
  }

  var extras = [];
  var queryEmptyNames = [];
  var formEmptyNames = [];
  var appQuery = opts.appQuery === undefined ? query : opts.appQuery;
  if (appQuery !== null && appQuery !== undefined && appQuery.length > 0) {
    var qb = Buffer.from(appQuery, 'latin1');
    forEachPiece(qb, 0, tick, function (start, end) {
      addExtras(appQuery.slice(start, end), false, known, extras, queryEmptyNames);
    });
  }
  if (form && formUntil > 0) {
    var formText = form.slice(0, formUntil);
    forEachPiece(formText, 0, tick, function (start, end) {
      var text = form.toString(latin1 ? 'latin1' : 'utf8', Math.max(start, bomEnd), end);
      addExtras(text, latin1, known, extras, formEmptyNames);
    });
  }

  var entries = [];
  map.forEach(function (values, name) {
    entries.push([name, values]);
  });
  return {
    entries: entries,
    extras: extras,
    queryEmptyNames: queryEmptyNames,
    formEmptyNames: formEmptyNames,
    deferred: deferredFrom === -1 ? null
      : { form: form, start: deferredFrom, latin1: latin1, bomEnd: bomEnd, known: known }
  };
}

/**
 * The extras of the body pairs Tomcat did not keep: every string of their application views that is not a string
 * of the Tomcat map. Calls `onExtra(name, value)` for each, in pair order, and `onEmptyName(value)` for each value of
 * a pair whose querystring name is empty; `tick` every 256 pairs.
 */
function deferredExtras(deferred, tick, onExtra, onEmptyName) {
  var form = deferred.form;
  forEachPiece(form, deferred.start, tick, function (start, end) {
    var extras = [];
    var empty = [];
    var text = form.toString(deferred.latin1 ? 'latin1' : 'utf8', Math.max(start, deferred.bomEnd), end);
    addExtras(text, deferred.latin1, deferred.known, extras, empty);
    for (var i = 0; i < extras.length; i++) {
      if (onExtra(extras[i].name, extras[i].value) === false) {
        return false;
      }
    }
    for (var e = 0; e < empty.length; e++) {
      if (onEmptyName(empty[e]) === false) {
        return false;
      }
    }
    return true;
  });
}

module.exports = {
  MAX_PARAMETER_COUNT: MAX_PARAMETER_COUNT,
  MAX_FORM_BYTES: MAX_FORM_BYTES,
  parameterMap: parameterMap,
  deferredExtras: deferredExtras,
  rawQuery: rawQuery,
  tomcatDecode: tomcatDecode,
  qsDecode: qsDecode,
  querystringDecode: querystringDecode
};
