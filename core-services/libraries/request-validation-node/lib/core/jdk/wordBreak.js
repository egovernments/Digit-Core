'use strict';

// The Final_Cased condition of Java 8's String.toLowerCase(Locale.ROOT) for U+03A3: within the word that contains
// the sigma, a cased code point precedes it and none follows it. Words are the segments of Java 8's word
// BreakIterator (root locale). Segments are found by longest match from the start of the text over these patterns,
// where a code point of general category Cf other than U+00AD is skipped (it joins the segment around it):
//   any single code point;
//   [word] (number word)* [number [post-number]]       word: letters with single mid-word marks between them and
//   pre-number (number word)* [number [post-number]]          an optional danda at the end; number: digits with
//                                                             single mid-number marks between them
//   whitespace* [CR] [line separator];  katakana runs;  hiragana runs;  kanji runs;  base mark mark*
// A letter, digit or whitespace code point may be followed by marks (Mn, Me). Character classes come from the
// general categories of the Java 8 character data.

// Character.getType constants.
var NON_SPACING_MARK = 6;
var ENCLOSING_MARK = 7;
var COMBINING_SPACING_MARK = 8;
var DECIMAL_DIGIT_NUMBER = 9;
var OTHER_NUMBER = 11;
var SPACE_SEPARATOR = 12;
var LINE_SEPARATOR = 13;
var PARAGRAPH_SEPARATOR = 14;
var CONTROL = 15;
var FORMAT = 16;
var DASH_PUNCTUATION = 20;
var CONNECTOR_PUNCTUATION = 23;
var CURRENCY_SYMBOL = 26;

// Class bits.
var IGNORE = 1;
var MARK = 2;
var LETTER = 4;
var DIGIT = 8;
var MID_WORD = 16;
var MID_NUMBER = 32;
var PRE_NUMBER = 64;
var POST_NUMBER = 128;
var LINE_END = 256;
var SPACE = 512;
var CR = 1024;
var DANDA = 2048;
var KANJI = 4096;
var KATAKANA = 8192;
var HIRAGANA = 16384;
var KANA_MARK = 32768;
var BASE = 65536;

function isKanji(cp) {
  return cp === 0x3005 || (cp >= 0x4E00 && cp <= 0x9FA5) || (cp >= 0xF900 && cp <= 0xFA2D);
}

function isKatakana(cp) {
  return (cp >= 0x30A1 && cp <= 0x30FA) || cp === 0x30FD || cp === 0x30FE;
}

function isHiragana(cp) {
  return (cp >= 0x3041 && cp <= 0x3094) || cp === 0x309D || cp === 0x309E;
}

function isKanaMark(cp) {
  return (cp >= 0x3099 && cp <= 0x309C) || cp === 0x30FB || cp === 0x30FC;
}

function classOf(cp, chars) {
  if (cp === 0x00AD) {
    return MID_WORD; // listed as a mid-word mark, which takes it out of the skipped format characters
  }
  var type = chars.generalCategory(cp);
  if (type === FORMAT) {
    return IGNORE;
  }
  var bits = 0;
  if (type === NON_SPACING_MARK || type === ENCLOSING_MARK) {
    bits |= MARK;
  } else if (type !== CONTROL && type !== LINE_SEPARATOR && type !== PARAGRAPH_SEPARATOR) {
    bits |= BASE;
  }
  var cjk = false;
  if (isKanji(cp)) {
    bits |= KANJI;
    cjk = true;
  }
  if (isKatakana(cp)) {
    bits |= KATAKANA;
    cjk = true;
  }
  if (isHiragana(cp)) {
    bits |= HIRAGANA;
    cjk = true;
  }
  if (isKanaMark(cp)) {
    bits |= KANA_MARK;
    cjk = true;
  }
  if (!cjk && ((type >= 1 && type <= 5) || type === COMBINING_SPACING_MARK)) {
    bits |= LETTER;
  }
  if (type >= DECIMAL_DIGIT_NUMBER && type <= OTHER_NUMBER) {
    bits |= DIGIT;
  }
  if (type === DASH_PUNCTUATION || type === CONNECTOR_PUNCTUATION || cp === 0x00AD || cp === 0x2027 || cp === 0x22
      || cp === 0x27 || cp === 0x2E) {
    bits |= MID_WORD;
  }
  if (cp === 0x22 || cp === 0x27 || cp === 0x2C || cp === 0x066B || cp === 0x2E) {
    bits |= MID_NUMBER;
  }
  if ((type === CURRENCY_SYMBOL || cp === 0x23 || cp === 0x2E) && cp !== 0x00A2) {
    bits |= PRE_NUMBER;
  }
  if (cp === 0x25 || cp === 0x26 || cp === 0x00A2 || cp === 0x066A || cp === 0x2030 || cp === 0x2031) {
    bits |= POST_NUMBER;
  }
  if (cp === 0x0A || cp === 0x0C || cp === 0x2028 || cp === 0x2029) {
    bits |= LINE_END;
  }
  if (type === SPACE_SEPARATOR || cp === 0x09) {
    bits |= SPACE;
  }
  if (cp === 0x0D) {
    bits |= CR;
  }
  if (cp === 0x0964 || cp === 0x0965) {
    bits |= DANDA;
  }
  return bits;
}

function codePointAt(s, i) {
  var c = s.charCodeAt(i);
  if (c >= 0xD800 && c <= 0xDBFF && i + 1 < s.length) {
    var d = s.charCodeAt(i + 1);
    if (d >= 0xDC00 && d <= 0xDFFF) {
      return ((c - 0xD800) << 10) + (d - 0xDC00) + 0x10000;
    }
  }
  return c;
}

// States of the word/number pattern (both its forms): 0 start, 1 in a word, 2 after a mid-word mark, 3 after a
// danda, 4 in a number, 5 after a mid-number mark, 6 after a post-number mark, 7 after a pre-number mark; -1 dead.
var ACCEPTING_ALNUM = [false, true, false, true, true, false, true, true];

function stepAlnum(state, bits) {
  switch (state) {
    case 0:
      return (bits & LETTER) ? 1 : (bits & DIGIT) ? 4 : (bits & PRE_NUMBER) ? 7 : -1;
    case 1:
      if (bits & (MARK | LETTER)) {
        return 1;
      }
      return (bits & MID_WORD) ? 2 : (bits & DANDA) ? 3 : (bits & DIGIT) ? 4 : -1;
    case 2:
      return (bits & LETTER) ? 1 : -1;
    case 3:
    case 7:
      return (bits & DIGIT) ? 4 : -1;
    case 4:
      if (bits & (MARK | DIGIT)) {
        return 4;
      }
      return (bits & MID_NUMBER) ? 5 : (bits & LETTER) ? 1 : (bits & POST_NUMBER) ? 6 : -1;
    case 5:
      return (bits & DIGIT) ? 4 : -1;
    default:
      return -1;
  }
}

// whitespace* [CR] [line end]: 0 start, 1 in spaces, 2 after CR, 3 after the line end.
function stepSpace(state, bits) {
  if ((state === 0 || state === 1) && (bits & SPACE)) {
    return 1;
  }
  if (state === 1 && (bits & MARK)) {
    return 1;
  }
  if ((state === 0 || state === 1) && (bits & CR)) {
    return 2;
  }
  if (state <= 2 && state >= 0 && (bits & LINE_END)) {
    return 3;
  }
  return -1;
}

/** The end of the word-boundary segment that starts at `start`. */
function segmentEnd(s, start, chars) {
  var n = s.length;
  var accept = start + (codePointAt(s, start) > 0xFFFF ? 2 : 1);
  var any = 0;      // the single-code-point pattern: 0 start, 1 matched, -1 dead
  var alnum = 0;
  var space = 0;
  var kata = 0;     // 0 start, 1 in the run, -1 dead (same for hira and kanji)
  var hira = 0;
  var kanji = 0;
  var marked = 0;   // base mark mark*: 0 start, 1 after the base, 2 after a mark, -1 dead
  var accepting = false;
  var i = start;
  while (i < n) {
    var cp = codePointAt(s, i);
    var width = cp > 0xFFFF ? 2 : 1;
    var bits = classOf(cp, chars);
    if (bits === IGNORE) {
      if (accepting) {
        accept = i + width;
      }
      i += width;
      continue;
    }
    any = any === 0 ? 1 : -1;
    if (alnum >= 0) {
      alnum = stepAlnum(alnum, bits);
    }
    if (space >= 0) {
      space = stepSpace(space, bits);
    }
    kata = kata >= 0 && (bits & (KATAKANA | KANA_MARK)) ? 1 : -1;
    hira = hira >= 0 && (bits & (HIRAGANA | KANA_MARK)) ? 1 : -1;
    kanji = kanji >= 0 && (bits & KANJI) ? 1 : -1;
    if (marked === 0) {
      marked = (bits & BASE) ? 1 : -1;
    } else if (marked > 0) {
      marked = (bits & MARK) ? 2 : -1;
    }
    if (any < 0 && alnum < 0 && space < 0 && kata < 0 && hira < 0 && kanji < 0 && marked < 0) {
      break;
    }
    accepting = any === 1 || (alnum >= 0 && ACCEPTING_ALNUM[alnum]) || space > 0 || kata === 1 || hira === 1
        || kanji === 1 || marked === 2;
    if (accepting) {
      accept = i + width;
    }
    i += width;
  }
  return accept;
}

function isHigh(c) {
  return c >= 0xD800 && c <= 0xDBFF;
}

function isLow(c) {
  return c >= 0xDC00 && c <= 0xDFFF;
}

/**
 * A word iterator over `s` answering isBoundary(offset) the way Java 8's does. isBoundary(i) asks for the first
 * boundary after i - 1: from the last boundary found when that lies before i - 1, else from a boundary found by
 * scanning backwards. When i - 1 is the second unit of a surrogate pair, that backward scan stops after the two
 * units and steps forward over the pair, so the position after the pair counts as a boundary unless the pair starts
 * the text.
 */
function BoundaryIterator(s, chars) {
  this.s = s;
  this.chars = chars;
  this.cached = -1;
  this.boundaries = null;
}

BoundaryIterator.prototype.next = function (pos) {
  return pos >= this.s.length ? -1 : segmentEnd(this.s, pos, this.chars);
};

// The last boundary at or before `offset` in the segmentation from the start of the text.
BoundaryIterator.prototype.safeBefore = function (offset) {
  if (this.boundaries === null) {
    this.boundaries = [0];
    for (var b = 0; b < this.s.length;) {
      b = segmentEnd(this.s, b, this.chars);
      this.boundaries.push(b);
    }
  }
  var best = 0;
  for (var i = 0; i < this.boundaries.length && this.boundaries[i] <= offset; i++) {
    best = this.boundaries[i];
  }
  return best;
};

BoundaryIterator.prototype.following = function (offset) {
  var s = this.s;
  if (offset === 0) {
    this.cached = this.next(0);
    return this.cached;
  }
  var result = this.cached;
  if (result >= offset || result < 0) {
    if (isLow(s.charCodeAt(offset)) && isHigh(s.charCodeAt(offset - 1))) {
      result = offset - 1 > 0 ? offset + 1 : 0;
    } else {
      result = this.safeBefore(offset);
    }
  }
  while (result !== -1 && result <= offset) {
    result = this.next(result);
  }
  this.cached = result;
  return result;
};

BoundaryIterator.prototype.isBoundary = function (offset) {
  return offset === 0 || this.following(offset - 1) === offset;
};

function codePointBefore(s, i) {
  var c = s.charCodeAt(i - 1);
  if (isLow(c) && i - 2 >= 0 && isHigh(s.charCodeAt(i - 2))) {
    return ((s.charCodeAt(i - 2) - 0xD800) << 10) + (c - 0xDC00) + 0x10000;
  }
  return c;
}

/**
 * Whether the code point at `index` of `s` meets Final_Cased (ConditionalSpecialCasing): walking back from it to
 * the previous word boundary finds a cased code point, and walking forward to the next boundary finds none. The
 * boundary questions are asked in Java's order of one iterator. `chars` provides generalCategory and isCased.
 */
function isFinalCased(s, index, chars) {
  var words = new BoundaryIterator(s, chars);
  var ch;
  for (var i = index; i >= 0 && !words.isBoundary(i); i -= ch > 0xFFFF ? 2 : 1) {
    ch = codePointBefore(s, i);
    if (chars.isCased(ch)) {
      var len = s.length;
      for (i = index + (codePointAt(s, index) > 0xFFFF ? 2 : 1); i < len && !words.isBoundary(i);
        i += ch > 0xFFFF ? 2 : 1) {
        ch = codePointAt(s, i);
        if (chars.isCased(ch)) {
          return false;
        }
      }
      return true;
    }
  }
  return false;
}

module.exports = {
  isFinalCased: isFinalCased,
  segmentEnd: segmentEnd
};
