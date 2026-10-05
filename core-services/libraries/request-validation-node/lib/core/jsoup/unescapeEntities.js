'use strict';

// Port of jsoup 1.17.2 Parser.unescapeEntities(value, false): Tokeniser.unescapeEntities and
// Tokeniser.consumeCharacterReference over a CharacterReader. jsoup is MIT licensed; see NOTICE.
//
// The CharacterReader reads its input through a 32 KiB window (readerPos/bufLength/bufPos/bufSplitPoint/bufMark
// below). Scans for digits and entity names stop at the end of the window, so a run of digits that crosses it is
// cut there, exactly as in jsoup. Inputs up to 32,768 units always fit in one window.

var chars = require('../jdk/javaChars');
var TABLES = require('../../../data/jsoup-entities.json');

var MAX_BUFFER_LEN = 32768;                            // CharacterReader.maxBufferLen
var READ_AHEAD_LIMIT = Math.floor(MAX_BUFFER_LEN * 0.75); // CharacterReader.readAheadLimit
var MIN_READ_AHEAD_LEN = 1024;                         // CharacterReader.minReadAheadLen
var EOF = 0xFFFF;                                      // CharacterReader.EOF, (char) -1

// Tokeniser.win1252Extensions: numeric references 0x80-0x9F map through Windows-1252.
var WIN1252 = [
  0x20AC, 0x0081, 0x201A, 0x0192, 0x201E, 0x2026, 0x2020, 0x2021,
  0x02C6, 0x2030, 0x0160, 0x2039, 0x0152, 0x008D, 0x017D, 0x008F,
  0x0090, 0x2018, 0x2019, 0x201C, 0x201D, 0x2022, 0x2013, 0x2014,
  0x02DC, 0x2122, 0x0161, 0x203A, 0x0153, 0x009D, 0x017E, 0x0178
];

function toText(codePoints) {
  var out = '';
  for (var i = 0; i < codePoints.length; i++) {
    out += chars.codePointToString(codePoints[i]);
  }
  return out;
}

// Name -> decoded text. Both tables hold the text jsoup emits (Entities.codepointsForName).
var BASE = new Map();
var FULL = new Map();
Object.keys(TABLES.base).forEach(function (name) {
  BASE.set(name, toText(TABLES.base[name]));
});
Object.keys(TABLES.full).forEach(function (name) {
  FULL.set(name, toText(TABLES.full[name]));
});
if (BASE.size !== TABLES.baseCount || FULL.size !== TABLES.fullCount) {
  throw new Error('jsoup entity tables are incomplete');
}

function isAsciiDigit(c) {
  return c >= 0x30 && c <= 0x39;
}

function isHexDigit(c) {
  return (c >= 0x30 && c <= 0x39) || (c >= 0x41 && c <= 0x46) || (c >= 0x61 && c <= 0x66);
}

function isNameLetter(c) {
  return (c >= 0x41 && c <= 0x5A) || (c >= 0x61 && c <= 0x7A) || chars.isLetter(c);
}

// Tokeniser.notCharRefCharsSorted: '\t', '\n', '\r', '\f', ' ', '<', '&'.
function isNotCharRefChar(c) {
  return c === 0x09 || c === 0x0A || c === 0x0D || c === 0x0C || c === 0x20 || c === 0x3C || c === 0x26;
}

/** Integer.valueOf(digits, radix) as jsoup uses it: -1 when the value does not fit an int. */
function parseReference(digits, radix) {
  var value = 0;
  for (var i = 0; i < digits.length; i++) {
    var c = digits.charCodeAt(i);
    var d = c <= 0x39 ? c - 0x30 : (c | 0x20) - 0x61 + 10;
    value = value * radix + d;
    if (value > 0x7FFFFFFF) {
      return -1;
    }
  }
  return value;
}

function unescapeEntities(input) {
  if (typeof input !== 'string') {
    throw new TypeError('value must be a string');
  }
  var s = input;
  var len = s.length;
  if (s.indexOf('&') === -1) {
    return s;
  }

  // CharacterReader state; absolute index of charBuf[i] is readerPos + i.
  var readerPos = 0;
  var bufLength = 0;
  var bufPos = 0;
  var bufSplitPoint = 0;
  var bufMark = -1;
  var readFully = false;
  // Cache of the last '&' search: no '&' in [ampFrom, ampAt) and s[ampAt] is '&' (ampAt -1: none at or after ampFrom).
  var ampFrom = -1;
  var ampAt = -1;

  function bufferUp() {
    if (readFully || bufPos < bufSplitPoint) {
      return;
    }
    var pos = bufMark !== -1 ? bufMark : bufPos;
    var offset = bufPos - pos;
    var from = readerPos + pos;
    var remaining = len - from;
    if (remaining <= 0) {
      readFully = true; // the read returns -1 and nothing is buffered
      return;
    }
    var read = remaining < MAX_BUFFER_LEN ? remaining : MAX_BUFFER_LEN;
    if (remaining <= MIN_READ_AHEAD_LEN) {
      readFully = true; // a second read was attempted and returned -1
    }
    bufLength = read;
    readerPos = from;
    bufPos = offset;
    if (bufMark !== -1) {
      bufMark = 0;
    }
    bufSplitPoint = bufLength < READ_AHEAD_LIMIT ? bufLength : READ_AHEAD_LIMIT;
  }

  function isEmpty() {
    bufferUp();
    return bufPos >= bufLength;
  }

  function at(index) {
    return s.charCodeAt(readerPos + index);
  }

  function nextIndexOfAmp() {
    bufferUp();
    var from = readerPos + bufPos;
    var found;
    if (ampFrom !== -1 && ampFrom <= from && (ampAt === -1 || ampAt >= from)) {
      found = ampAt;
    } else {
      found = s.indexOf('&', from);
      ampFrom = from;
      ampAt = found;
    }
    if (found === -1 || found >= readerPos + bufLength) {
      return -1;
    }
    return found - from;
  }

  // consumeToAmp(): moves the reader to the next '&' in the buffered window, or to the end of the window. The text
  // passed over is copied to the output later, as a slice of the input.
  function skipToAmp() {
    var offset = nextIndexOfAmp();
    if (offset !== -1) {
      bufPos += offset;
      return;
    }
    bufferUp();
    bufPos = bufLength;
  }

  function matchesChar(c) {
    return !isEmpty() && at(bufPos) === c;
  }

  function consume() {
    bufferUp();
    var value = bufPos >= bufLength ? EOF : at(bufPos);
    bufPos++;
    return value;
  }

  function mark() {
    if (bufLength - bufPos < MIN_READ_AHEAD_LEN) {
      bufSplitPoint = 0;
    }
    bufferUp();
    bufMark = bufPos;
  }

  function unmark() {
    bufMark = -1;
  }

  function rewindToMark() {
    if (bufMark === -1) {
      throw new Error('Mark invalid');
    }
    bufPos = bufMark;
    unmark();
  }

  // matchConsume(String) for a one-character sequence.
  function matchConsume(c) {
    bufferUp();
    bufferUp(); // matches(String) buffers up again
    if (1 > bufLength - bufPos || at(bufPos) !== c) {
      return false;
    }
    bufPos += 1;
    return true;
  }

  // matchConsumeIgnoreCase("X"): Character.toUpperCase(c) == 'X'.
  function matchConsumeIgnoreCaseX() {
    bufferUp();
    if (1 > bufLength - bufPos || !chars.isUpperCaseX(at(bufPos))) {
      return false;
    }
    bufPos += 1;
    return true;
  }

  function matchesNotCharRef() {
    bufferUp();
    return !isEmpty() && isNotCharRefChar(at(bufPos));
  }

  function consumeWhile(predicate) {
    bufferUp();
    var start = bufPos;
    while (bufPos < bufLength && predicate(at(bufPos))) {
      bufPos++;
    }
    return s.slice(readerPos + start, readerPos + bufPos);
  }

  function consumeLetterThenDigitSequence() {
    bufferUp();
    var start = bufPos;
    while (bufPos < bufLength && isNameLetter(at(bufPos))) {
      bufPos++;
    }
    while (bufPos < bufLength && isAsciiDigit(at(bufPos))) {
      bufPos++;
    }
    return s.slice(readerPos + start, readerPos + bufPos);
  }

  // Tokeniser.consumeCharacterReference(null, false); returns the decoded text or null.
  function consumeCharacterReference() {
    if (isEmpty()) {
      return null;
    }
    if (matchesNotCharRef()) {
      return null;
    }
    mark();
    if (matchConsume(0x23)) { // '#'
      var hexMode = matchConsumeIgnoreCaseX();
      var digits = hexMode ? consumeWhile(isHexDigit) : consumeWhile(isAsciiDigit);
      if (digits.length === 0) {
        rewindToMark();
        return null;
      }
      unmark();
      matchConsume(0x3B); // ';' is optional
      var value = parseReference(digits, hexMode ? 16 : 10);
      if (value === -1 || value > 0x10FFFF) {
        return '\uFFFD';
      }
      if (value >= 0x80 && value < 0x80 + WIN1252.length) {
        value = WIN1252[value - 0x80];
      }
      return chars.codePointToString(value);
    }
    var name = consumeLetterThenDigitSequence();
    var looksLegit = matchesChar(0x3B);
    var found = BASE.has(name) || (looksLegit && FULL.has(name));
    if (!found) {
      rewindToMark();
      return null;
    }
    unmark();
    matchConsume(0x3B);
    return FULL.get(name);
  }

  // CharacterReader constructor
  bufferUp();
  // The output is the input with each decoded reference replaced: unchanged text is copied as slices of the input
  // (from copyFrom), and the input itself is returned when nothing was decoded.
  var pieces = null;
  var copyFrom = 0;
  while (!isEmpty()) {
    skipToAmp();
    if (matchesChar(0x26)) {
      var amp = readerPos + bufPos;
      consume();
      var decoded = consumeCharacterReference();
      if (decoded !== null) {
        if (pieces === null) {
          pieces = [];
        }
        if (amp > copyFrom) {
          pieces.push(s.slice(copyFrom, amp));
        }
        pieces.push(decoded);
        copyFrom = readerPos + bufPos;
      }
    }
  }
  if (pieces === null) {
    return s;
  }
  if (copyFrom < len) {
    pieces.push(s.slice(copyFrom));
  }
  return pieces.join('');
}

function isBaseNamedEntity(name) {
  return BASE.has(name);
}

function isNamedEntity(name) {
  return FULL.has(name);
}

module.exports = {
  unescapeEntities: unescapeEntities,
  isBaseNamedEntity: isBaseNamedEntity,
  isNamedEntity: isNamedEntity,
  BASE_COUNT: TABLES.baseCount,
  FULL_COUNT: TABLES.fullCount
};
