'use strict';

// Java UTF-8 decoding behaviour, implemented from its observable rules:
//  - decodeLossy: new String(bytes, UTF_8); every malformed sequence becomes one U+FFFD.
//  - isValid: strict decoding succeeds (a sequence is malformed exactly where decodeLossy emits a replacement for it).
//  - readerView: what a Jackson ReaderBasedJsonParser reading 4,000 UTF-16 units per read() call receives from an
//    InputStreamReader with a REPORT decoder (malformed input throws in the read that meets it, and that read's
//    units are lost).

var REPLACEMENT = 0xFFFD;
var READ_UNITS = 4000;   // jackson-core BufferRecycler CHAR_TOKEN_BUFFER length, the size of every read() call
var BYTE_CHUNK = 8192;   // the decoder's byte buffer: bytes visible to the decoder at once

function isCont(b) {
  return (b & 0xC0) === 0x80;
}

// Second byte check for a 3-byte lead: E0 needs A0-BF, the others 80-BF.
function badThreeSecond(b1, b2) {
  return (b1 === 0xE0 && (b2 & 0xE0) === 0x80) || !isCont(b2);
}

// Second byte check for a 4-byte lead: F0 needs 90-BF, F4 needs 80-8F, the others 80-BF.
function badFourSecond(b1, b2) {
  return (b1 === 0xF0 && (b2 < 0x90 || b2 > 0xBF)) || (b1 === 0xF4 && (b2 & 0xF0) !== 0x80) || !isCont(b2);
}

/**
 * Scans one sequence at bytes[i] (lead >= 0x80) with `end` as the end of input.
 * Returns the number of bytes consumed; sets scan.unit / scan.unit2 (or scan.unit = -1 for a replacement) and
 * scan.stop (true when the replacement swallows the rest of the input).
 */
var scan = { unit: 0, unit2: -1, stop: false };
function scanSequence(bytes, i, end) {
  var b1 = bytes[i];
  var follow = end - i - 1;
  scan.unit2 = -1;
  scan.stop = false;
  if (b1 < 0xC2 || b1 > 0xF7) {           // 80-C1, F8-FF
    scan.unit = -1;
    return 1;
  }
  if (b1 < 0xE0) {                         // C2-DF
    if (follow < 1) {
      scan.unit = -1;
      scan.stop = true;
      return 1;
    }
    var c2 = bytes[i + 1];
    if (!isCont(c2)) {
      scan.unit = -1;
      return 1;
    }
    scan.unit = ((b1 & 0x1F) << 6) | (c2 & 0x3F);
    return 2;
  }
  if (b1 < 0xF0) {                         // E0-EF
    if (follow >= 2) {
      var e2 = bytes[i + 1];
      var e3 = bytes[i + 2];
      if (badThreeSecond(b1, e2)) {
        scan.unit = -1;
        return 1;
      }
      if (!isCont(e3)) {
        scan.unit = -1;
        return 2;
      }
      var u = ((b1 & 0x0F) << 12) | ((e2 & 0x3F) << 6) | (e3 & 0x3F);
      scan.unit = u >= 0xD800 && u <= 0xDFFF ? -1 : u;
      return 3;
    }
    scan.unit = -1;
    if (follow === 1 && badThreeSecond(b1, bytes[i + 1])) {
      return 1;
    }
    scan.stop = true;
    return 1;
  }
  // F0-F7
  if (follow >= 3) {
    var f2 = bytes[i + 1];
    var f3 = bytes[i + 2];
    var f4 = bytes[i + 3];
    var cp = ((b1 & 0x07) << 18) | ((f2 & 0x3F) << 12) | ((f3 & 0x3F) << 6) | (f4 & 0x3F);
    if (!isCont(f2) || !isCont(f3) || !isCont(f4) || cp < 0x10000 || cp > 0x10FFFF) {
      scan.unit = -1;
      if (b1 > 0xF4 || badFourSecond(b1, f2)) {
        return 1;
      }
      return isCont(f3) ? 3 : 2;
    }
    var v = cp - 0x10000;
    scan.unit = 0xD800 + (v >> 10);
    scan.unit2 = 0xDC00 + (v & 0x3FF);
    return 4;
  }
  scan.unit = -1;
  if (b1 > 0xF4 || (follow >= 1 && badFourSecond(b1, bytes[i + 1]))) {
    return 1;
  }
  if (follow >= 2 && !isCont(bytes[i + 2])) {
    return 2;
  }
  scan.stop = true;
  return 1;
}

/**
 * new String(bytes, StandardCharsets.UTF_8) for bytes[start, end): Java's replacement rules, not Node's.
 * `bytes` is a Buffer, Uint8Array or array of byte values.
 */
function decodeLossy(bytes, start, end) {
  var from = start === undefined ? 0 : start;
  var to = end === undefined ? bytes.length : end;
  var units = [];
  var out = '';
  var i = from;
  while (i < to) {
    var b = bytes[i];
    if (b < 0x80) {
      units.push(b);
      i++;
    } else {
      var consumed = scanSequence(bytes, i, to);
      if (scan.unit < 0) {
        units.push(REPLACEMENT);
      } else {
        units.push(scan.unit);
        if (scan.unit2 >= 0) {
          units.push(scan.unit2);
        }
      }
      if (scan.stop) {
        break;
      }
      i += consumed;
    }
    if (units.length >= 8192) {
      out += String.fromCharCode.apply(null, units);
      units.length = 0;
    }
  }
  return units.length ? out + String.fromCharCode.apply(null, units) : out;
}

/** Index of the first byte of the first malformed sequence in bytes[start, end), or -1 when strictly valid. */
function firstMalformed(bytes, start, end) {
  var from = start === undefined ? 0 : start;
  var to = end === undefined ? bytes.length : end;
  var i = from;
  while (i < to) {
    if (bytes[i] < 0x80) {
      i++;
      continue;
    }
    var consumed = scanSequence(bytes, i, to);
    if (scan.unit < 0) {
      return i;
    }
    i += consumed;
  }
  return -1;
}

/** True when bytes[start, end) is well-formed UTF-8 by Java's strict rules. */
function isValid(bytes, start, end) {
  return firstMalformed(bytes, start, end) === -1;
}

// Result codes of one decoder step.
var UNDERFLOW = 0;
var OVERFLOW = 1;
var MALFORMED = 2;

/**
 * The text a 4,000-unit-per-read Reader delivers from buf[offset, buf.length) before strict decoding fails.
 *
 * Returns { text, availEnd, failAtEnd }:
 *  - valid input: the whole text, availEnd = text.length, failAtEnd = false;
 *  - invalid input: text = the units delivered by every read before the read that fails, availEnd = text.length,
 *    failAtEnd = true (the next access past availEnd is the failing read).
 *
 * Failure timing follows the decoder: a read fails when it meets a malformed sequence; a sequence that does not fit
 * the read's remaining room ends the read, and the next read meets it again; a sequence cut by the end of input
 * fails the read that meets it with nothing delivered, otherwise the following read. The decoder sees at most 8,192
 * bytes at a time, refilled from its current position when it runs out; when a read is full, an early malformation
 * check can only use bytes already in that window.
 */
function readerView(buf, offset) {
  var start = offset === undefined ? 0 : offset;
  var len = buf.length;
  if (firstMalformed(buf, start, len) === -1) {
    var whole = buf.toString('utf8', start, len);
    return { text: whole, availEnd: whole.length, failAtEnd: false };
  }
  var pos = start;        // next byte the decoder reads
  var windowEnd = start;  // end of the bytes currently visible to the decoder
  var room = 0;           // units still free in the current read
  var step = function (atEof) {
    while (pos < windowEnd) {
      var b1 = buf[pos];
      var avail = windowEnd - pos;
      if (b1 < 0x80) {
        if (room < 1) {
          return OVERFLOW;
        }
        room--;
        pos++;
      } else if (b1 >= 0xC2 && b1 <= 0xDF) {
        if (avail < 2 || room < 1) {
          return avail < 2 ? (atEof ? MALFORMED : UNDERFLOW) : OVERFLOW;
        }
        if (!isCont(buf[pos + 1])) {
          return MALFORMED;
        }
        room--;
        pos += 2;
      } else if (b1 >= 0xE0 && b1 <= 0xEF) {
        if (avail < 3 || room < 1) {
          if (avail > 1 && badThreeSecond(b1, buf[pos + 1])) {
            return MALFORMED;
          }
          return avail < 3 ? (atEof ? MALFORMED : UNDERFLOW) : OVERFLOW;
        }
        var b2 = buf[pos + 1];
        var b3 = buf[pos + 2];
        if (badThreeSecond(b1, b2) || !isCont(b3)) {
          return MALFORMED;
        }
        var u = ((b1 & 0x0F) << 12) | ((b2 & 0x3F) << 6) | (b3 & 0x3F);
        if (u >= 0xD800 && u <= 0xDFFF) {
          return MALFORMED;
        }
        room--;
        pos += 3;
      } else if (b1 >= 0xF0 && b1 <= 0xF7) {
        if (avail < 4 || room < 2) {
          if (b1 > 0xF4 || (avail > 1 && badFourSecond(b1, buf[pos + 1]))) {
            return MALFORMED;
          }
          if (avail > 2 && !isCont(buf[pos + 2])) {
            return MALFORMED;
          }
          return avail < 4 ? (atEof ? MALFORMED : UNDERFLOW) : OVERFLOW;
        }
        var c2 = buf[pos + 1];
        var c3 = buf[pos + 2];
        var c4 = buf[pos + 3];
        var cp = ((b1 & 0x07) << 18) | ((c2 & 0x3F) << 12) | ((c3 & 0x3F) << 6) | (c4 & 0x3F);
        if (badFourSecond(b1, c2) || !isCont(c3) || !isCont(c4) || cp < 0x10000 || cp > 0x10FFFF) {
          return MALFORMED;
        }
        room -= 2;
        pos += 4;
      } else {
        return MALFORMED;
      }
    }
    return UNDERFLOW;
  };

  for (;;) {
    var readStart = pos;
    room = READ_UNITS;
    var eof = false;
    var failed = false;
    for (;;) {
      var result = step(eof);
      if (result === MALFORMED) {
        failed = true;
        break;
      }
      if (result === OVERFLOW) {
        break;
      }
      // Underflow: the decoder needs more bytes.
      if (eof || room === 0) {
        break;
      }
      if (room < READ_UNITS && windowEnd >= len) {
        break; // nothing more is available now; return what this read has
      }
      if (windowEnd >= len) {
        eof = true;
        if (pos === windowEnd) {
          break; // end of input with nothing pending
        }
        continue; // pending bytes at end of input are malformed
      }
      windowEnd = Math.min(len, pos + BYTE_CHUNK);
    }
    if (failed) {
      var text = buf.toString('utf8', start, readStart);
      return { text: text, availEnd: text.length, failAtEnd: true };
    }
    if (room === READ_UNITS) {
      // End of input without a failure cannot happen for invalid input; treat it as the end of the text.
      var all = buf.toString('utf8', start, pos);
      return { text: all, availEnd: all.length, failAtEnd: false };
    }
  }
}

module.exports = {
  decodeLossy: decodeLossy,
  isValid: isValid,
  firstMalformed: firstMalformed,
  readerView: readerView,
  READ_UNITS: READ_UNITS,
  BYTE_CHUNK: BYTE_CHUNK
};
