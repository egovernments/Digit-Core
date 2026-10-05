'use strict';

// Java 8 character predicates and case mappings, read from data/java8-chars.json (generated on the reference
// Java 8 runtime by tools/GenerateTables.java). Every function takes UTF-16 code units or code points as numbers.

var DATA = require('../../../data/java8-chars.json');
var wordBreak = require('./wordBreak');

var LETTER = 1;
var WHITESPACE = 2;
var IDENTIFIER_PART = 4;

var bmpFlags = new Uint8Array(0x10000);
function markRanges(ranges, flag) {
  for (var i = 0; i < ranges.length; i++) {
    for (var c = ranges[i][0]; c <= ranges[i][1]; c++) {
      bmpFlags[c] |= flag;
    }
  }
}
markRanges(DATA.letter, LETTER);
markRanges(DATA.javaIdentifierPart, IDENTIFIER_PART);
for (var w = 0; w < DATA.whitespace.length; w++) {
  bmpFlags[DATA.whitespace[w]] |= WHITESPACE;
}

// Character.isDefined(int) over all code points, as a bit set.
var definedBits = new Uint8Array(0x110000 >> 3);
for (var d = 0; d < DATA.defined.length; d++) {
  for (var cp = DATA.defined[d][0]; cp <= DATA.defined[d][1]; cp++) {
    definedBits[cp >> 3] |= 1 << (cp & 7);
  }
}

// Character.getType(int) of every code point: run starts and their types, searched by bisection.
var categoryStarts = new Int32Array(DATA.generalCategory.length);
var categoryTypes = new Uint8Array(DATA.generalCategory.length);
for (var g = 0; g < DATA.generalCategory.length; g++) {
  categoryStarts[g] = DATA.generalCategory[g][0];
  categoryTypes[g] = DATA.generalCategory[g][1];
}

// ConditionalSpecialCasing.isCased(int) (the Final_Cased condition of String.toLowerCase): ranges.
var casedStarts = new Int32Array(DATA.cased.length);
var casedEnds = new Int32Array(DATA.cased.length);
for (var k = 0; k < DATA.cased.length; k++) {
  casedStarts[k] = DATA.cased[k][0];
  casedEnds[k] = DATA.cased[k][1];
}

// Character.digit(char, 36) of the non-ASCII units that are digits: [first, last, value of first].
var DIGIT_ROWS = DATA.digit;

var nfkcOpaque = new Set(DATA.nfkcOpaque);
var upperX = new Set(DATA.toUpperCaseIsX);

// Character.toLowerCase(char): only the units that change.
var lowerChar = new Map();
for (var l = 0; l < DATA.lowerChar.length; l++) {
  lowerChar.set(DATA.lowerChar[l][0], DATA.lowerChar[l][1]);
}

// new String(Character.toChars(cp)).toLowerCase(Locale.ROOT): only the code points that change.
var lowerRoot = new Map();
for (var r = 0; r < DATA.lowerRoot.length; r++) {
  var row = DATA.lowerRoot[r];
  lowerRoot.set(row[0], fromCodePoints(row, 1));
}

function fromCodePoints(values, start) {
  var out = '';
  for (var i = start; i < values.length; i++) {
    out += codePointToString(values[i]);
  }
  return out;
}

/** StringBuilder.appendCodePoint semantics: BMP values (surrogates included) become one unit. */
function codePointToString(cp) {
  if (cp < 0x10000) {
    return String.fromCharCode(cp);
  }
  var v = cp - 0x10000;
  return String.fromCharCode(0xD800 + (v >> 10), 0xDC00 + (v & 0x3FF));
}

/** Character.isLetter(char). */
function isLetter(unit) {
  return (bmpFlags[unit] & LETTER) !== 0;
}

/** Character.isWhitespace(char). */
function isWhitespace(unit) {
  return (bmpFlags[unit] & WHITESPACE) !== 0;
}

/** Character.isJavaIdentifierPart(char). */
function isJavaIdentifierPart(unit) {
  return (bmpFlags[unit] & IDENTIFIER_PART) !== 0;
}

/** Character.isISOControl(char): U+0000-U+001F and U+007F-U+009F. */
function isISOControl(unit) {
  return unit <= 0x1F || (unit >= 0x7F && unit <= 0x9F);
}

// The index of the last start <= value in a sorted Int32Array, or -1.
function lastAtOrBelow(starts, value) {
  var lo = 0;
  var hi = starts.length - 1;
  var found = -1;
  while (lo <= hi) {
    var mid = (lo + hi) >>> 1;
    if (starts[mid] <= value) {
      found = mid;
      lo = mid + 1;
    } else {
      hi = mid - 1;
    }
  }
  return found;
}

/** Character.getType(int): the general category constant of a code point. */
function generalCategory(cp) {
  var i = lastAtOrBelow(categoryStarts, cp);
  return i < 0 ? 0 : categoryTypes[i];
}

/** The isCased test of String.toLowerCase's Final_Cased condition. */
function isCased(cp) {
  var i = lastAtOrBelow(casedStarts, cp);
  return i >= 0 && cp <= casedEnds[i];
}

/** Character.digit(char, radix) for 2 <= radix <= 36: the digit value of a UTF-16 unit, or -1. */
function digit(unit, radix) {
  var value = -1;
  if (unit < 0x80) {
    if (unit >= 0x30 && unit <= 0x39) {
      value = unit - 0x30;
    } else if (unit >= 0x61 && unit <= 0x7A) {
      value = unit - 0x61 + 10;
    } else if (unit >= 0x41 && unit <= 0x5A) {
      value = unit - 0x41 + 10;
    }
  } else {
    for (var i = 0; i < DIGIT_ROWS.length; i++) {
      var row = DIGIT_ROWS[i];
      if (unit >= row[0] && unit <= row[1]) {
        value = row[2] + (unit - row[0]);
        break;
      }
    }
  }
  return value < radix ? value : -1;
}

/** Character.isDefined(int). */
function isDefined(cp) {
  return cp >= 0 && cp <= 0x10FFFF && (definedBits[cp >> 3] & (1 << (cp & 7))) !== 0;
}

/** Code points that Character.isDefined reports but the Java 8 normalizer does not decompose. */
function isNfkcOpaque(cp) {
  return nfkcOpaque.has(cp);
}

/** Character.toUpperCase(char) == 'X'. */
function isUpperCaseX(unit) {
  return upperX.has(unit);
}

/** Character.toLowerCase(char). */
function toLowerCaseChar(unit) {
  if (unit < 0x80) {
    return unit >= 0x41 && unit <= 0x5A ? unit + 0x20 : unit;
  }
  var mapped = lowerChar.get(unit);
  return mapped === undefined ? unit : mapped;
}

/** new String(Character.toChars(cp)).toLowerCase(Locale.ROOT), as a string. */
function lowerRootCodePoint(cp) {
  var mapped = lowerRoot.get(cp);
  return mapped === undefined ? codePointToString(cp) : mapped;
}

/**
 * String.toLowerCase(Locale.ROOT), applied per code point (a high surrogate followed by a low surrogate is one code
 * point). U+03A3 becomes U+03C2 when it ends a word that has a cased letter before it (Final_Cased, with Java 8's
 * word boundaries), else U+03C3.
 */
function toLowerCaseRoot(value) {
  var out = '';
  var start = 0;
  var n = value.length;
  for (var i = 0; i < n; i++) {
    var c = value.charCodeAt(i);
    var cp = c;
    var width = 1;
    if (c >= 0xD800 && c <= 0xDBFF && i + 1 < n) {
      var next = value.charCodeAt(i + 1);
      if (next >= 0xDC00 && next <= 0xDFFF) {
        cp = ((c - 0xD800) << 10) + (next - 0xDC00) + 0x10000;
        width = 2;
      }
    }
    var mapped;
    if (cp < 0x80) {
      mapped = cp >= 0x41 && cp <= 0x5A ? String.fromCharCode(cp + 0x20) : undefined;
    } else if (cp === 0x03A3) {
      mapped = wordBreak.isFinalCased(value, i, module.exports) ? '\u03C2' : '\u03C3';
    } else {
      mapped = lowerRoot.get(cp);
    }
    if (mapped !== undefined) {
      out += value.slice(start, i) + mapped;
      start = i + width;
    }
    i += width - 1;
  }
  return start === 0 ? value : out + value.slice(start);
}

/** String.trim(): strips units <= U+0020 at both ends. */
function javaTrim(value) {
  var start = 0;
  var end = value.length;
  while (start < end && value.charCodeAt(start) <= 0x20) {
    start++;
  }
  while (end > start && value.charCodeAt(end - 1) <= 0x20) {
    end--;
  }
  return start === 0 && end === value.length ? value : value.slice(start, end);
}

module.exports = {
  JAVA_VERSION: DATA.javaVersion,
  isLetter: isLetter,
  isWhitespace: isWhitespace,
  isJavaIdentifierPart: isJavaIdentifierPart,
  isISOControl: isISOControl,
  isDefined: isDefined,
  generalCategory: generalCategory,
  isCased: isCased,
  digit: digit,
  isNfkcOpaque: isNfkcOpaque,
  isUpperCaseX: isUpperCaseX,
  toLowerCaseChar: toLowerCaseChar,
  lowerRootCodePoint: lowerRootCodePoint,
  toLowerCaseRoot: toLowerCaseRoot,
  javaTrim: javaTrim,
  codePointToString: codePointToString
};
