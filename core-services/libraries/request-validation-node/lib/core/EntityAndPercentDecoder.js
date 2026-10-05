'use strict';

// Port of EntityAndPercentDecoder: the two decoders behind the detector's variants, and their cheap pre-filters.

var chars = require('./jdk/javaChars');
var utf8 = require('./jdk/utf8');
var jsoup = require('./jsoup/unescapeEntities');

function hex(c) {
  if (c >= 0x30 && c <= 0x39) {
    return c - 0x30;
  }
  if (c >= 0x61 && c <= 0x66) {
    return c - 0x61 + 10;
  }
  if (c >= 0x41 && c <= 0x46) {
    return c - 0x41 + 10;
  }
  return -1;
}

/** A '%' at `index` followed by two hex digits, with index + 2 < value.length. */
function isEscapeAt(value, index) {
  return index + 2 < value.length
      && value.charCodeAt(index) === 0x25
      && hex(value.charCodeAt(index + 1)) >= 0
      && hex(value.charCodeAt(index + 2)) >= 0;
}

/** False only when decodeEntities cannot change the value: jsoup needs '&' then '#' or a letter. */
function mayDecodeEntities(value) {
  var n = value.length;
  for (var index = value.indexOf('&'); index >= 0 && index + 1 < n; index = value.indexOf('&', index + 1)) {
    var next = value.charCodeAt(index + 1);
    if (next === 0x23 || chars.isLetter(next)) {
      return true;
    }
  }
  return false;
}

/** False only when decodePercent cannot change the value: it needs '%' then two hex digits. */
function mayDecodePercent(value) {
  for (var index = value.indexOf('%'); index >= 0; index = value.indexOf('%', index + 1)) {
    if (isEscapeAt(value, index)) {
      return true;
    }
  }
  return false;
}

/** jsoup Parser.unescapeEntities(value, false). */
function decodeEntities(value) {
  return jsoup.unescapeEntities(value);
}

function nextEscape(value, from) {
  for (var index = value.indexOf('%', from); index >= 0; index = value.indexOf('%', index + 1)) {
    if (isEscapeAt(value, index)) {
      return index;
    }
  }
  return -1;
}

/**
 * Replaces each maximal run of %XX escapes by the Java lossy UTF-8 decoding of its bytes (invalid bytes become
 * U+FFFD while valid bytes in the same run still decode). Everything else is copied unchanged.
 */
function decodePercent(value) {
  var output = '';
  var index = 0;
  var n = value.length;
  while (index < n) {
    var escape = nextEscape(value, index);
    if (escape === -1) {
      return output + value.slice(index);
    }
    output += value.slice(index, escape);
    index = escape;
    var bytes = [];
    while (isEscapeAt(value, index)) {
      bytes.push((hex(value.charCodeAt(index + 1)) << 4) | hex(value.charCodeAt(index + 2)));
      index += 3;
    }
    output += utf8.decodeLossy(bytes);
  }
  return output;
}

module.exports = {
  mayDecodeEntities: mayDecodeEntities,
  mayDecodePercent: mayDecodePercent,
  decodeEntities: decodeEntities,
  decodePercent: decodePercent,
  isEscapeAt: isEscapeAt,
  hex: hex
};
