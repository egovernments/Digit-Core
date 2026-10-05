'use strict';

// The subset of jackson-core 2.18.6 ReaderBasedJsonParser that the JSON inspector relies on, with Jackson's timing:
//  - input arrives in reads of 4,000 UTF-16 units (3,999 when a surrogate pair would straddle the end of a read);
//    a read that meets malformed UTF-8 fails, so the failure surfaces exactly when the parser first needs a unit at
//    or past the end of the last successful read;
//  - the call that returns FIELD_NAME also skips the colon and pre-parses the value (numbers completely, literals
//    including the identifier check after them, containers and strings only recognised);
//  - string values are scanned only when their text is requested (getText);
//  - StreamReadConstraints (nesting depth, string length, number length, the built-in 50,000-unit name limit) fire
//    where Jackson checks them, including the incremental string-length checks of its segmented text buffer;
//  - default parser features only (no comments, no single quotes, no unquoted names, no leading zeros, no NaN).
// Errors: JsonParseError (malformed input or a failed read) and StreamConstraintsError (a limit).
//
// Derived from jackson-core 2.18.6, Apache License 2.0 (see NOTICE): modified by translation to JavaScript and by
// reduction to the behaviour described above.

var javaChars = require('../jdk/javaChars');
var SymbolTable = require('./SymbolTable');

var StreamConstraintsError = SymbolTable.StreamConstraintsError;

/** Jackson's JsonParseException or a read failure (any IOException that is not a constraint). */
class JsonParseError extends Error {
  constructor(message) {
    super(message);
  }
}

Object.defineProperty(JsonParseError.prototype, 'name', {
  value: 'JsonParseError',
  writable: true,
  configurable: true
});

// Token ids
var START_OBJECT = 1;
var END_OBJECT = 2;
var START_ARRAY = 3;
var END_ARRAY = 4;
var FIELD_NAME = 5;
var VALUE_STRING = 6;
var VALUE_NUMBER_INT = 7;
var VALUE_NUMBER_FLOAT = 8;
var VALUE_TRUE = 9;
var VALUE_FALSE = 10;
var VALUE_NULL = 11;

// Parsing context types
var TYPE_ROOT = 0;
var TYPE_ARRAY = 1;
var TYPE_OBJECT = 2;

var READ_UNITS = 4000;              // BufferRecycler CHAR_TOKEN_BUFFER: the length of every read
var TEXT_BUFFER_DEFAULT = 200;      // BufferRecycler CHAR_TEXT_BUFFER default
var MIN_SEGMENT_LEN = 500;          // TextBuffer
var MAX_SEGMENT_LEN = 0x10000;      // TextBuffer

// CharTypes hex values: Jackson looks up (ch & 0xFF), so any unit whose low byte is a hex digit character counts.
var HEX = new Int8Array(256);
(function () {
  var i;
  for (i = 0; i < 256; i++) {
    HEX[i] = -1;
  }
  for (i = 0; i < 10; i++) {
    HEX[0x30 + i] = i;
  }
  for (i = 0; i < 6; i++) {
    HEX[0x61 + i] = 10 + i;
    HEX[0x41 + i] = 10 + i;
  }
}());

function parseError(message) {
  return new JsonParseError(message);
}

function isDigit(c) {
  return c >= 0x30 && c <= 0x39;
}

/**
 * Length bookkeeping of Jackson's segmented TextBuffer (only lengths: the text itself is taken from the input).
 * The incremental string-length checks happen when a segment is finished, so they depend on segment sizes, which
 * start from the recycled text buffer of the thread (none for a fresh thread). `pool` emulates that recycled buffer:
 * { size } (0 = none).
 */
function TextBuffer(maxStringLength, pool) {
  this.maxStringLength = maxStringLength;
  this.pool = pool;
  this.shared = false;      // _inputStart >= 0
  this.inputLen = 0;
  this.segLen = 0;          // length of _currentSegment, 0 when there is none
  this.currentSize = 0;
  this.segmentSize = 0;
  this.hasSegments = false;
}

TextBuffer.prototype.validate = function (length) {
  if (length > this.maxStringLength) {
    throw new StreamConstraintsError('String value length exceeds the maximum allowed');
  }
};

// BufferRecycler.allocCharBuffer(CHAR_TEXT_BUFFER, needed): takes the recycled buffer when it is large enough
// (the slot is emptied either way), else allocates max(needed, 200).
TextBuffer.prototype.buf = function (needed) {
  var minSize = needed < TEXT_BUFFER_DEFAULT ? TEXT_BUFFER_DEFAULT : needed;
  var recycled = this.pool.size;
  this.pool.size = 0;
  return recycled >= minSize ? recycled : minSize;
};

function grow(oldLen) {
  var newLen = oldLen + (oldLen >> 1);
  if (newLen < MIN_SEGMENT_LEN) {
    return MIN_SEGMENT_LEN;
  }
  return newLen > MAX_SEGMENT_LEN ? MAX_SEGMENT_LEN : newLen;
}

TextBuffer.prototype.clearSegments = function () {
  this.hasSegments = false;
  this.currentSize = 0;
  this.segmentSize = 0;
};

TextBuffer.prototype.resetWithShared = function (len) {
  this.shared = true;
  this.inputLen = len;
  if (this.hasSegments) {
    this.clearSegments();
  }
};

TextBuffer.prototype.resetWithCopy = function (len) {
  this.shared = false;
  this.inputLen = 0;
  if (this.hasSegments) {
    this.clearSegments();
  } else if (this.segLen === 0) {
    this.segLen = this.buf(len);
  }
  this.currentSize = 0;
  this.segmentSize = 0;
  this.append(len);
};

TextBuffer.prototype.unshare = function (needExtra) {
  var sharedLen = this.inputLen;
  this.inputLen = 0;
  this.shared = false;
  var needed = sharedLen + needExtra;
  if (this.segLen === 0 || needed > this.segLen) {
    this.segLen = this.buf(needed);
  }
  this.segmentSize = 0;
  this.currentSize = sharedLen;
};

TextBuffer.prototype.expand = function () {
  this.hasSegments = true;
  this.segmentSize += this.segLen;
  this.currentSize = 0;
  this.segLen = grow(this.segLen);
};

TextBuffer.prototype.finishCurrentSegment = function () {
  this.hasSegments = true;
  this.segmentSize += this.segLen;
  this.currentSize = 0;
  this.validate(this.segmentSize);
  this.segLen = grow(this.segLen);
};

// append(char[], start, len)
TextBuffer.prototype.append = function (len) {
  if (this.shared) {
    this.unshare(len);
  }
  var max = this.segLen - this.currentSize;
  if (max >= len) {
    this.currentSize += len;
    return;
  }
  var total = this.segmentSize + this.currentSize + len;
  this.validate(total > 0x7FFFFFFF ? 0x7FFFFFFF : total);
  if (max > 0) {
    len -= max;
  }
  do {
    this.expand();
    var amount = this.segLen < len ? this.segLen : len;
    this.currentSize += amount;
    len -= amount;
  } while (len > 0);
};

TextBuffer.prototype.getCurrentSegment = function () {
  if (this.shared) {
    this.unshare(1);
  } else if (this.segLen === 0) {
    this.segLen = this.buf(0);
  } else if (this.currentSize >= this.segLen) {
    this.expand();
  }
};

TextBuffer.prototype.emptyAndGetCurrentSegment = function () {
  this.shared = false;
  this.currentSize = 0;
  this.inputLen = 0;
  if (this.hasSegments) {
    this.clearSegments();
  }
  if (this.segLen === 0) {
    this.segLen = this.buf(0);
  }
};

TextBuffer.prototype.size = function () {
  return this.shared ? this.inputLen : this.segmentSize + this.currentSize;
};

// The checks contentsAsString() makes.
TextBuffer.prototype.contentsAsString = function () {
  if (this.shared) {
    if (this.inputLen >= 1) {
      this.validate(this.inputLen);
    }
  } else if (this.segmentSize === 0) {
    if (this.currentSize !== 0) {
      this.validate(this.currentSize);
    }
  } else {
    this.validate(this.segmentSize + this.currentSize);
  }
};

// The checks getTextBuffer() makes (field names): only an aggregated multi-segment name is length-checked.
TextBuffer.prototype.getTextBuffer = function () {
  if (!this.shared && this.hasSegments) {
    var size = this.segmentSize + this.currentSize;
    if (size >= 1) {
      this.validate(size);
    }
  }
};

// Releasing the parser returns its current segment to the recycler when that makes the slot larger.
TextBuffer.prototype.release = function () {
  if (this.segLen !== 0 && this.segLen > this.pool.size) {
    this.pool.size = this.segLen;
  }
  this.segLen = 0;
};

function Context(type, parent) {
  this.type = type;
  this.parent = parent;
  this.depth = parent === null ? 0 : parent.depth + 1;
  this.index = -1;
}

/**
 * @param {{text: string, availEnd: number, failAtEnd: boolean}} view decoded input (jdk/utf8.readerView)
 * @param {object} options
 * @param {number} options.maxNestingDepth StreamReadConstraints maxNestingDepth
 * @param {number} options.maxStringLength StreamReadConstraints maxStringLength
 * @param {number} options.maxNumberLength StreamReadConstraints maxNumberLength
 * @param {number} [options.seed] symbol-table seed (random when omitted)
 * @param {{size: number}} [options.textBufferPool] recycled text buffer of the emulated thread (fresh when omitted)
 */
function Tokenizer(view, options) {
  if (view === null || typeof view !== 'object' || typeof view.text !== 'string') {
    throw new TypeError('view');
  }
  var opts = options || {};
  this._text = view.text;
  this._availEnd = view.availEnd;
  this._failAtEnd = view.failAtEnd === true;
  this._inputPtr = 0;
  this._inputEnd = 0;
  this._closed = false;
  this._maxDepth = opts.maxNestingDepth;
  this._maxNumberLength = opts.maxNumberLength;
  this._tb = new TextBuffer(opts.maxStringLength, opts.textBufferPool || { size: 0 });
  this._symbols = new SymbolTable(opts.seed);
  this._ctx = new Context(TYPE_ROOT, null);
  this._currToken = null;
  this._nextToken = 0;
  this._tokenIncomplete = false;
  this._name = null;
  this._stringValue = null;
  this._numberLength = 0;
}

// _loadMore(): the next read. Throws when that read fails; false at the end of input.
Tokenizer.prototype._loadMore = function () {
  if (this._closed) {
    return false;
  }
  var start = this._inputEnd;
  if (start >= this._availEnd) {
    if (this._failAtEnd) {
      throw parseError('Invalid UTF-8 input');
    }
    this._inputPtr = this._availEnd;
    this._inputEnd = this._availEnd;
    this._closed = true;
    return false;
  }
  var n = this._availEnd - start;
  if (n >= READ_UNITS) {
    n = READ_UNITS;
    var last = this._text.charCodeAt(start + READ_UNITS - 1);
    if (last >= 0xD800 && last <= 0xDBFF) {
      n = READ_UNITS - 1; // the decoder will not split a surrogate pair across reads
    }
  }
  this._inputPtr = start;
  this._inputEnd = start + n;
  return true;
};

Tokenizer.prototype._needMore = function () {
  return this._inputPtr < this._inputEnd || this._loadMore();
};

/** The next token id, or null at the end of input. */
Tokenizer.prototype.nextToken = function () {
  if (this._currToken === FIELD_NAME) {
    return this._nextAfterName();
  }
  if (this._tokenIncomplete) {
    this._skipString();
  }
  var i = this._skipWSOrEnd();
  if (i < 0) {
    this._close();
    this._currToken = null;
    return null;
  }
  if ((i | 0x20) === 0x7D) { // '}' or ']'
    this._closeScope(i);
    return this._currToken;
  }
  var ctx = this._ctx;
  ctx.index++;
  if (ctx.type !== TYPE_ROOT && ctx.index > 0) {
    i = this._skipComma(i);
  }
  var inObject = ctx.type === TYPE_OBJECT;
  if (inObject) {
    if (i !== 0x22) {
      throw parseError('Unexpected character: was expecting double-quote to start field name');
    }
    this._name = this._parseName();
    this._currToken = FIELD_NAME;
    i = this._skipColon();
  }
  var t;
  switch (i) {
    case 0x22:
      this._tokenIncomplete = true;
      t = VALUE_STRING;
      break;
    case 0x5B:
      if (!inObject) {
        this._createChildContext(TYPE_ARRAY);
      }
      t = START_ARRAY;
      break;
    case 0x7B:
      if (!inObject) {
        this._createChildContext(TYPE_OBJECT);
      }
      t = START_OBJECT;
      break;
    case 0x74:
      this._matchLiteral('true');
      t = VALUE_TRUE;
      break;
    case 0x66:
      this._matchLiteral('false');
      t = VALUE_FALSE;
      break;
    case 0x6E:
      this._matchLiteral('null');
      t = VALUE_NULL;
      break;
    case 0x2D:
      t = this._parseSignedNumber();
      break;
    case 0x30: case 0x31: case 0x32: case 0x33: case 0x34:
    case 0x35: case 0x36: case 0x37: case 0x38: case 0x39:
      t = this._parseUnsignedNumber(i);
      break;
    default:
      // '}' ("expected a value"), '+', '.', ']', ',', NaN, Infinity and anything else: errors with default features
      throw parseError('Unexpected character: expected a valid value');
  }
  if (inObject) {
    this._nextToken = t;
    return FIELD_NAME;
  }
  this._currToken = t;
  return t;
};

Tokenizer.prototype._nextAfterName = function () {
  var t = this._nextToken;
  this._nextToken = 0;
  if (t === START_ARRAY) {
    this._createChildContext(TYPE_ARRAY);
  } else if (t === START_OBJECT) {
    this._createChildContext(TYPE_OBJECT);
  }
  this._currToken = t;
  return t;
};

Tokenizer.prototype._createChildContext = function (type) {
  var child = new Context(type, this._ctx);
  this._ctx = child;
  if (child.depth > this._maxDepth) {
    throw new StreamConstraintsError('Document nesting depth exceeds the maximum allowed');
  }
};

Tokenizer.prototype._closeScope = function (i) {
  var ctx = this._ctx;
  if (i === 0x5D) {
    if (ctx.type !== TYPE_ARRAY) {
      throw parseError('Unexpected close marker');
    }
    this._ctx = ctx.parent;
    this._currToken = END_ARRAY;
  } else {
    if (ctx.type !== TYPE_OBJECT) {
      throw parseError('Unexpected close marker');
    }
    this._ctx = ctx.parent;
    this._currToken = END_OBJECT;
  }
};

Tokenizer.prototype._close = function () {
  this._closed = true;
  this._tb.release();
};

/** Releases the text buffer to the emulated thread's recycler (what closing the parser does). */
Tokenizer.prototype.close = function () {
  if (this._tb.segLen !== 0) {
    this._tb.release();
  }
  this._closed = true;
};

// ---------------------------------------------------------------------------------------------------------------
// White space, comma, colon

function invalidSpace() {
  return parseError('Illegal character: only regular white space is allowed between tokens');
}

// _skipCR(): Jackson looks at the unit after a CR (which may need a read).
Tokenizer.prototype._skipCR = function () {
  if (this._needMore()) {
    if (this._text.charCodeAt(this._inputPtr) === 0x0A) {
      this._inputPtr++;
    }
  }
};

// One white space unit (<= 0x20) that is not a space: LF and TAB pass, CR peeks, anything else fails.
Tokenizer.prototype._space = function (i) {
  if (i === 0x0D) {
    this._skipCR();
  } else if (i !== 0x0A && i !== 0x09) {
    throw invalidSpace();
  }
};

Tokenizer.prototype._skipWSOrEnd = function () {
  var text = this._text;
  for (;;) {
    if (this._inputPtr >= this._inputEnd && !this._loadMore()) {
      if (this._ctx.type !== TYPE_ROOT) {
        throw parseError('Unexpected end-of-input: expected close marker');
      }
      return -1;
    }
    var i = text.charCodeAt(this._inputPtr++);
    if (i > 0x20) {
      if (i === 0x2F) {
        throw parseError('Unexpected character: comments are not enabled');
      }
      return i; // '#' is returned as a token start (YAML comments are not enabled)
    }
    if (i !== 0x20) {
      this._space(i);
    }
  }
};

Tokenizer.prototype._skipComma = function (i) {
  if (i !== 0x2C) {
    throw parseError('Unexpected character: was expecting comma to separate entries');
  }
  var text = this._text;
  for (;;) {
    if (this._inputPtr >= this._inputEnd && !this._loadMore()) {
      throw parseError('Unexpected end-of-input within/between entries');
    }
    var c = text.charCodeAt(this._inputPtr++);
    if (c > 0x20) {
      if (c === 0x2F) {
        throw parseError('Unexpected character: comments are not enabled');
      }
      return c;
    }
    if (c !== 0x20) {
      this._space(c);
    }
  }
};

// Skips white space, the colon and white space; returns the first unit of the value.
Tokenizer.prototype._skipColon = function () {
  var text = this._text;
  var gotColon = false;
  for (;;) {
    if (this._inputPtr >= this._inputEnd && !this._loadMore()) {
      throw parseError('Unexpected end-of-input within/between entries');
    }
    var i = text.charCodeAt(this._inputPtr++);
    if (i > 0x20) {
      if (i === 0x2F) {
        throw parseError('Unexpected character: comments are not enabled');
      }
      if (gotColon) {
        return i;
      }
      if (i !== 0x3A) {
        throw parseError('Unexpected character: was expecting a colon to separate field name and value');
      }
      gotColon = true;
    } else if (i !== 0x20) {
      this._space(i);
    }
  }
};

// ---------------------------------------------------------------------------------------------------------------
// Literals

// _matchTrue/_matchFalse/_matchNull and _matchToken/_matchToken2/_checkMatchEnd. The first letter was consumed.
// After the literal Jackson looks at the next unit (reading more input if needed; the end of input is fine): a unit
// >= '0' other than ']' and '}' that is a Java 8 identifier part makes the token invalid.
Tokenizer.prototype._matchLiteral = function (word) {
  var text = this._text;
  for (var k = 1; k < word.length; k++) {
    if ((this._inputPtr >= this._inputEnd && !this._loadMore())
        || text.charCodeAt(this._inputPtr) !== word.charCodeAt(k)) {
      throw parseError('Unrecognized token: was expecting a literal');
    }
    this._inputPtr++;
  }
  if (this._inputPtr >= this._inputEnd && !this._loadMore()) {
    return;
  }
  var ch = text.charCodeAt(this._inputPtr);
  if (ch >= 0x30 && ch !== 0x5D && ch !== 0x7D && javaChars.isJavaIdentifierPart(ch)) {
    throw parseError('Unrecognized token: was expecting a literal');
  }
};

// ---------------------------------------------------------------------------------------------------------------
// Numbers

function numberTooLong() {
  return new StreamConstraintsError('Number value length exceeds the maximum allowed');
}

// _verifyRootSpace: a root number must be followed by white space (the caller has the unit at _inputPtr).
Tokenizer.prototype._verifyRootSpace = function (ch) {
  this._inputPtr++;
  if (ch === 0x20 || ch === 0x09) {
    return;
  }
  if (ch === 0x0D) {
    this._inputPtr--;
    return;
  }
  if (ch === 0x0A) {
    return;
  }
  throw parseError('Expected space separating root-level values');
};

Tokenizer.prototype._resetInt = function (intLen, textLength) {
  if (intLen > this._maxNumberLength) {
    throw numberTooLong();
  }
  this._numberLength = textLength;
  return VALUE_NUMBER_INT;
};

Tokenizer.prototype._resetFloat = function (intLen, fractLen, expLen, textLength) {
  if (intLen + fractLen + expLen > this._maxNumberLength) {
    throw numberTooLong();
  }
  this._numberLength = textLength;
  return VALUE_NUMBER_FLOAT;
};

// _parseUnsignedNumber: the first digit was consumed. Leading '0' and numbers that reach the end of the current
// read go through _parseNumber2.
Tokenizer.prototype._parseUnsignedNumber = function (first) {
  var text = this._text;
  var ptr = this._inputPtr;
  var startPtr = ptr - 1;
  var inputLen = this._inputEnd;
  if (first === 0x30) {
    return this._parseNumber2(false, startPtr);
  }
  var intLen = 1;
  var ch;
  for (;;) {
    if (ptr >= inputLen) {
      this._inputPtr = startPtr;
      return this._parseNumber2(false, startPtr);
    }
    ch = text.charCodeAt(ptr++);
    if (!isDigit(ch)) {
      break;
    }
    intLen++;
  }
  if (ch === 0x2E || (ch | 0x20) === 0x65) {
    this._inputPtr = ptr;
    return this._parseFloat(ch, startPtr, ptr, false, intLen);
  }
  ptr--;
  this._inputPtr = ptr;
  if (this._ctx.type === TYPE_ROOT) {
    this._verifyRootSpace(ch);
  }
  this._tb.resetWithShared(ptr - startPtr);
  return this._resetInt(intLen, ptr - startPtr);
};

// _parseSignedNumber(true): the '-' was consumed.
Tokenizer.prototype._parseSignedNumber = function () {
  var text = this._text;
  var ptr = this._inputPtr;
  var startPtr = ptr - 1;
  var inputEnd = this._inputEnd;
  if (ptr >= inputEnd) {
    return this._parseNumber2(true, startPtr);
  }
  var ch = text.charCodeAt(ptr++);
  if (!isDigit(ch)) {
    this._inputPtr = ptr;
    throw parseError('Unexpected character: expected digit (0-9) to follow minus sign');
  }
  if (ch === 0x30) {
    return this._parseNumber2(true, startPtr);
  }
  var intLen = 1;
  for (;;) {
    if (ptr >= inputEnd) {
      return this._parseNumber2(true, startPtr);
    }
    ch = text.charCodeAt(ptr++);
    if (!isDigit(ch)) {
      break;
    }
    intLen++;
  }
  if (ch === 0x2E || (ch | 0x20) === 0x65) {
    this._inputPtr = ptr;
    return this._parseFloat(ch, startPtr, ptr, true, intLen);
  }
  ptr--;
  this._inputPtr = ptr;
  if (this._ctx.type === TYPE_ROOT) {
    this._verifyRootSpace(ch);
  }
  this._tb.resetWithShared(ptr - startPtr);
  return this._resetInt(intLen, ptr - startPtr);
};

// _parseFloat (fast path): fraction and exponent lengths start at 0.
Tokenizer.prototype._parseFloat = function (ch, startPtr, ptr, neg, intLen) {
  var text = this._text;
  var inputLen = this._inputEnd;
  var fractLen = 0;
  if (ch === 0x2E) {
    for (;;) {
      if (ptr >= inputLen) {
        return this._parseNumber2(neg, startPtr);
      }
      ch = text.charCodeAt(ptr++);
      if (!isDigit(ch)) {
        break;
      }
      fractLen++;
    }
    if (fractLen === 0) {
      throw parseError('Unexpected character: Decimal point not followed by a digit');
    }
  }
  var expLen = 0;
  if ((ch | 0x20) === 0x65) {
    if (ptr >= inputLen) {
      this._inputPtr = startPtr;
      return this._parseNumber2(neg, startPtr);
    }
    ch = text.charCodeAt(ptr++);
    if (ch === 0x2D || ch === 0x2B) {
      if (ptr >= inputLen) {
        this._inputPtr = startPtr;
        return this._parseNumber2(neg, startPtr);
      }
      ch = text.charCodeAt(ptr++);
    }
    while (isDigit(ch)) {
      expLen++;
      if (ptr >= inputLen) {
        this._inputPtr = startPtr;
        return this._parseNumber2(neg, startPtr);
      }
      ch = text.charCodeAt(ptr++);
    }
    if (expLen === 0) {
      throw parseError('Unexpected character: Exponent indicator not followed by a digit');
    }
  }
  ptr--;
  this._inputPtr = ptr;
  if (this._ctx.type === TYPE_ROOT) {
    this._verifyRootSpace(ch);
  }
  this._tb.resetWithShared(ptr - startPtr);
  return this._resetFloat(intLen, fractLen, expLen, ptr - startPtr);
};

// _parseNumber2 (slow path): text goes through the segmented text buffer, and a missing fraction or exponent
// counts as -1 in the float length check.
Tokenizer.prototype._parseNumber2 = function (neg, startPtr) {
  var text = this._text;
  var tb = this._tb;
  this._inputPtr = neg ? startPtr + 1 : startPtr;
  tb.emptyAndGetCurrentSegment();
  var segLen = tb.segLen;
  // outPtr: units in the current segment; before a unit is appended to a full segment, the segment is finished
  var outPtr = neg ? 1 : 0;
  var c = this._nextNumberChar();
  if (c === 0x30) {
    c = this._verifyNoLeadingZeroes();
  }
  var eof = false;
  var intLen = 0;
  while (isDigit(c)) {
    intLen++;
    if (outPtr >= segLen) {
      tb.finishCurrentSegment();
      segLen = tb.segLen;
      outPtr = 0;
    }
    outPtr++;
    if (this._inputPtr >= this._inputEnd && !this._loadMore()) {
      c = 0;
      eof = true;
      break;
    }
    c = text.charCodeAt(this._inputPtr++);
  }
  if (intLen === 0) {
    throw parseError('Unexpected character: expected digit (0-9) for valid numeric value');
  }
  var fractLen = -1;
  if (c === 0x2E) {
    fractLen = 0;
    if (outPtr >= segLen) {
      tb.finishCurrentSegment();
      segLen = tb.segLen;
      outPtr = 0;
    }
    outPtr++;
    for (;;) {
      if (this._inputPtr >= this._inputEnd && !this._loadMore()) {
        eof = true;
        break;
      }
      c = text.charCodeAt(this._inputPtr++);
      if (!isDigit(c)) {
        break;
      }
      fractLen++;
      if (outPtr >= segLen) {
        tb.finishCurrentSegment();
        segLen = tb.segLen;
        outPtr = 0;
      }
      outPtr++;
    }
    if (fractLen === 0) {
      throw parseError('Unexpected character: Decimal point not followed by a digit');
    }
  }
  var expLen = -1;
  if ((c | 0x20) === 0x65) {
    expLen = 0;
    if (outPtr >= segLen) {
      tb.finishCurrentSegment();
      segLen = tb.segLen;
      outPtr = 0;
    }
    outPtr++;
    c = this._nextNumberChar();
    if (c === 0x2D || c === 0x2B) {
      if (outPtr >= segLen) {
        tb.finishCurrentSegment();
        segLen = tb.segLen;
        outPtr = 0;
      }
      outPtr++;
      c = this._nextNumberChar();
    }
    while (isDigit(c)) {
      expLen++;
      if (outPtr >= segLen) {
        tb.finishCurrentSegment();
        segLen = tb.segLen;
        outPtr = 0;
      }
      outPtr++;
      if (this._inputPtr >= this._inputEnd && !this._loadMore()) {
        eof = true;
        break;
      }
      c = text.charCodeAt(this._inputPtr++);
    }
    if (expLen === 0) {
      throw parseError('Unexpected character: Exponent indicator not followed by a digit');
    }
  }
  if (!eof) {
    this._inputPtr--;
    if (this._ctx.type === TYPE_ROOT) {
      this._verifyRootSpace(c);
    }
  }
  tb.currentSize = outPtr;
  var length = tb.size();
  if (fractLen < 0 && expLen < 0) {
    return this._resetInt(intLen, length);
  }
  return this._resetFloat(intLen, fractLen, expLen, length);
};

// getNextChar inside a number: a unit is required.
Tokenizer.prototype._nextNumberChar = function () {
  if (this._inputPtr >= this._inputEnd && !this._loadMore()) {
    throw parseError('Unexpected end-of-input in a number value');
  }
  return this._text.charCodeAt(this._inputPtr++);
};

// _verifyNoLeadingZeroes: after a '0', the next unit (read if needed) must not be a digit.
Tokenizer.prototype._verifyNoLeadingZeroes = function () {
  if (this._inputPtr >= this._inputEnd && !this._loadMore()) {
    return 0x30;
  }
  if (isDigit(this._text.charCodeAt(this._inputPtr))) {
    throw parseError('Invalid numeric value: Leading zeroes not allowed');
  }
  return 0x30;
};

// ---------------------------------------------------------------------------------------------------------------
// Names and strings

// _decodeEscaped: the backslash was consumed. Returns the decoded UTF-16 unit.
Tokenizer.prototype._decodeEscaped = function () {
  var text = this._text;
  if (this._inputPtr >= this._inputEnd && !this._loadMore()) {
    throw parseError('Unexpected end-of-input in character escape sequence');
  }
  var c = text.charCodeAt(this._inputPtr++);
  switch (c) {
    case 0x62: return 0x08;
    case 0x74: return 0x09;
    case 0x6E: return 0x0A;
    case 0x66: return 0x0C;
    case 0x72: return 0x0D;
    case 0x22:
    case 0x2F:
    case 0x5C:
      return c;
    case 0x75:
      break;
    default:
      throw parseError('Unrecognized character escape');
  }
  var value = 0;
  for (var i = 0; i < 4; i++) {
    if (this._inputPtr >= this._inputEnd && !this._loadMore()) {
      throw parseError('Unexpected end-of-input in character escape sequence');
    }
    var digit = HEX[text.charCodeAt(this._inputPtr++) & 0xFF];
    if (digit < 0) {
      throw parseError('Unexpected character: expected a hex-digit for character escape sequence');
    }
    value = (value << 4) | digit;
  }
  return value;
};

// _parseName: the opening quote was consumed. Names that contain an escape or a control unit, or that reach the
// end of the current read, take the segmented-buffer path.
Tokenizer.prototype._parseName = function () {
  var text = this._text;
  var symbols = this._symbols;
  var ptr = this._inputPtr;
  var end = this._inputEnd;
  var start = ptr;
  var hash = symbols.seed;
  while (ptr < end) {
    var ch = text.charCodeAt(ptr);
    if (ch <= 0x5C && (ch === 0x22 || ch === 0x5C || ch < 0x20)) {
      if (ch === 0x22) {
        this._inputPtr = ptr + 1;
        return symbols.findSymbol(text.slice(start, ptr), hash);
      }
      break;
    }
    hash = (Math.imul(hash, 33) + ch) | 0;
    ptr++;
  }
  this._inputPtr = ptr;
  return this._parseName2(start, hash);
};

Tokenizer.prototype._parseName2 = function (startPtr, hash) {
  var text = this._text;
  var tb = this._tb;
  tb.resetWithShared(this._inputPtr - startPtr);
  tb.getCurrentSegment();
  var outPtr = tb.currentSize;
  var segLen = tb.segLen;
  var name = '';
  var runStart = startPtr;
  for (;;) {
    if (this._inputPtr >= this._inputEnd && !this._loadMore()) {
      throw parseError('Unexpected end-of-input in field name');
    }
    var c = text.charCodeAt(this._inputPtr++);
    if (c <= 0x5C) {
      if (c === 0x5C) {
        name += text.slice(runStart, this._inputPtr - 1);
        c = this._decodeEscaped();
        name += String.fromCharCode(c);
        runStart = this._inputPtr;
      } else if (c <= 0x22) {
        if (c === 0x22) {
          name += text.slice(runStart, this._inputPtr - 1);
          break;
        }
        if (c < 0x20) {
          throw parseError('Illegal unquoted character: has to be escaped using backslash to be included in name');
        }
      }
    }
    hash = (Math.imul(hash, 33) + c) | 0;
    // TextBuffer: append, then finish a segment as soon as it is full
    outPtr++;
    if (outPtr >= segLen) {
      tb.finishCurrentSegment();
      segLen = tb.segLen;
      outPtr = 0;
    }
  }
  tb.currentSize = outPtr;
  tb.getTextBuffer();
  return this._symbols.findSymbol(name, hash);
};

/** The decoded text of the current VALUE_STRING (scans it on first use, as Jackson's getText()). */
Tokenizer.prototype.getText = function () {
  if (this._currToken !== VALUE_STRING) {
    throw new Error('getText() is only supported for VALUE_STRING');
  }
  if (this._tokenIncomplete) {
    this._tokenIncomplete = false;
    this._finishString();
  }
  this._tb.contentsAsString();
  return this._stringValue;
};

/** The text length of the current number token (Jackson getTextLength()). */
Tokenizer.prototype.getTextLength = function () {
  if (this._currToken !== VALUE_NUMBER_INT && this._currToken !== VALUE_NUMBER_FLOAT) {
    throw new Error('getTextLength() is only supported for numbers');
  }
  return this._numberLength;
};

/** The name of the current FIELD_NAME. */
Tokenizer.prototype.getCurrentName = function () {
  return this._name;
};

// _finishString: the fast path needs the closing quote in the current read and no escape or control unit before it.
Tokenizer.prototype._finishString = function () {
  var text = this._text;
  var start = this._inputPtr;
  var ptr = start;
  var end = this._inputEnd;
  while (ptr < end) {
    var ch = text.charCodeAt(ptr);
    if (ch <= 0x5C && (ch === 0x22 || ch === 0x5C || ch < 0x20)) {
      if (ch === 0x22) {
        this._tb.resetWithShared(ptr - start);
        this._stringValue = text.slice(start, ptr);
        this._inputPtr = ptr + 1;
        return;
      }
      break;
    }
    ptr++;
  }
  this._tb.resetWithCopy(ptr - start);
  this._inputPtr = ptr;
  this._finishString2(start);
};

Tokenizer.prototype._finishString2 = function (runStart) {
  var text = this._text;
  var tb = this._tb;
  tb.getCurrentSegment();
  var outPtr = tb.currentSize;
  var segLen = tb.segLen;
  var value = '';
  for (;;) {
    if (this._inputPtr >= this._inputEnd && !this._loadMore()) {
      throw parseError('Unexpected end-of-input: was expecting closing quote for a string value');
    }
    // the run of plain units up to the next quote, backslash, control unit or the end of the current read
    var ptr = this._inputPtr;
    var end = this._inputEnd;
    var c = -1;
    while (ptr < end) {
      c = text.charCodeAt(ptr);
      if (c <= 0x5C && (c === 0x22 || c === 0x5C || c < 0x20)) {
        break;
      }
      ptr++;
    }
    var run = ptr - this._inputPtr;
    // TextBuffer: before each unit is appended, a full segment is finished
    while (run > 0) {
      if (outPtr >= segLen) {
        tb.finishCurrentSegment();
        segLen = tb.segLen;
        outPtr = 0;
      }
      var room = segLen - outPtr;
      var take = room < run ? room : run;
      outPtr += take;
      run -= take;
    }
    this._inputPtr = ptr;
    if (ptr >= end) {
      continue; // read more
    }
    this._inputPtr++;
    if (c === 0x22) {
      value += text.slice(runStart, ptr);
      break;
    }
    if (c === 0x5C) {
      value += text.slice(runStart, ptr);
      var decoded = this._decodeEscaped();
      if (outPtr >= segLen) {
        tb.finishCurrentSegment();
        segLen = tb.segLen;
        outPtr = 0;
      }
      outPtr++;
      value += String.fromCharCode(decoded);
      runStart = this._inputPtr;
      continue;
    }
    throw parseError('Illegal unquoted character: has to be escaped using backslash to be included in string value');
  }
  tb.currentSize = outPtr;
  this._stringValue = value;
};

// _skipString: used only when a string value was not read before the next token (not by the inspector).
Tokenizer.prototype._skipString = function () {
  this._tokenIncomplete = false;
  var text = this._text;
  for (;;) {
    if (this._inputPtr >= this._inputEnd && !this._loadMore()) {
      throw parseError('Unexpected end-of-input: was expecting closing quote for a string value');
    }
    var c = text.charCodeAt(this._inputPtr++);
    if (c <= 0x5C) {
      if (c === 0x5C) {
        this._decodeEscaped();
      } else if (c <= 0x22) {
        if (c === 0x22) {
          return;
        }
        if (c < 0x20) {
          throw parseError('Illegal unquoted character');
        }
      }
    }
  }
};

Tokenizer.JsonParseError = JsonParseError;
Tokenizer.StreamConstraintsError = StreamConstraintsError;
Tokenizer.START_OBJECT = START_OBJECT;
Tokenizer.END_OBJECT = END_OBJECT;
Tokenizer.START_ARRAY = START_ARRAY;
Tokenizer.END_ARRAY = END_ARRAY;
Tokenizer.FIELD_NAME = FIELD_NAME;
Tokenizer.VALUE_STRING = VALUE_STRING;
Tokenizer.VALUE_NUMBER_INT = VALUE_NUMBER_INT;
Tokenizer.VALUE_NUMBER_FLOAT = VALUE_NUMBER_FLOAT;
Tokenizer.VALUE_TRUE = VALUE_TRUE;
Tokenizer.VALUE_FALSE = VALUE_FALSE;
Tokenizer.VALUE_NULL = VALUE_NULL;
Tokenizer.READ_UNITS = READ_UNITS;

module.exports = Tokenizer;
