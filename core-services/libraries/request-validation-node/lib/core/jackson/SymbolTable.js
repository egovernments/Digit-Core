'use strict';

// Port of the name canonicalizer that jackson-core 2.18.6 (CharsToNameCanonicalizer) gives every
// ReaderBasedJsonParser: a child of a fresh root table, so it starts empty and lives for one document. Only the
// behaviour that can change a verdict is kept: which names are new (only new names are length-checked), the
// collision-chain guard (a bucket that overflows twice between rehashes fails the parse), and the point where
// canonicalization is switched off. All hash arithmetic is Java int arithmetic.
//
// Derived from jackson-core 2.18.6, Apache License 2.0 (see NOTICE): modified by translation to JavaScript and by
// reduction to the behaviour described above.

var HASH_MULT = 33;
var DEFAULT_T_SIZE = 64;
var MAX_T_SIZE = 0x10000;
var MAX_COLL_CHAIN_LENGTH = 150;
var DEFAULT_MAX_NAME_LEN = 50000; // StreamReadConstraints default; the library does not configure it

/** Jackson's StreamConstraintsException: a configured or built-in parser limit was exceeded. */
class StreamConstraintsError extends Error {
  constructor(message) {
    super(message);
  }
}

Object.defineProperty(StreamConstraintsError.prototype, 'name', {
  value: 'StreamConstraintsError',
  writable: true,
  configurable: true
});

function Bucket(symbol, next) {
  this.symbol = symbol;
  this.next = next;
  this.length = next === null ? 1 : next.length + 1;
}

function newArray(size) {
  var array = new Array(size);
  for (var i = 0; i < size; i++) {
    array[i] = null;
  }
  return array;
}

/** A random seed with the range of System.identityHashCode on a 64-bit HotSpot VM: 1 .. 2^31 - 1. */
function randomSeed() {
  return Math.floor(Math.random() * 0x7FFFFFFF) + 1;
}

/** The parser's name hash: seed, then h * 33 + unit for every UTF-16 unit (no 0 -> 1 adjustment). */
function hashName(seed, name) {
  var h = seed | 0;
  for (var i = 0; i < name.length; i++) {
    h = (Math.imul(h, HASH_MULT) + name.charCodeAt(i)) | 0;
  }
  return h;
}

class SymbolTable {
  /**
   * @param {number} [seed] hash seed (an int); a random one when omitted
   * @param {number} [maxNameLength] the name length limit applied to new names (default 50,000)
   */
  constructor(seed, maxNameLength) {
    this.seed = seed === undefined || seed === null ? randomSeed() : seed | 0;
    this.maxNameLength = maxNameLength === undefined ? DEFAULT_MAX_NAME_LEN : maxNameLength;
    this.canonicalize = true;
    this.symbols = newArray(DEFAULT_T_SIZE);
    this.buckets = newArray(DEFAULT_T_SIZE >> 1);
    this.size = 0;
    this.sizeThreshold = DEFAULT_T_SIZE - (DEFAULT_T_SIZE >> 2);
    this.indexMask = DEFAULT_T_SIZE - 1;
    this.hashShared = true;
    this.overflows = null;
  }

  /** The hash the parser computes while reading a name. */
  hash(name) {
    return hashName(this.seed, name);
  }

  /** calcHash: the same fold, with 0 mapped to 1 (used when re-placing symbols). */
  calcHash(name) {
    var h = hashName(this.seed, name);
    return h === 0 ? 1 : h;
  }

  hashToIndex(rawHash) {
    var h = rawHash | 0;
    h = (h + (h >>> 15)) | 0;
    h ^= h << 7;
    h = (h + (h >>> 3)) | 0;
    return h & this.indexMask;
  }

  /**
   * findSymbol: returns the name, adding it when it is new. Throws StreamConstraintsError for a new name longer
   * than the name limit, and for a second collision-chain overflow of the same bucket.
   * @param {string} name the decoded name
   * @param {number} h the parser's hash of the name (see hash())
   */
  findSymbol(name, h) {
    var len = name.length;
    if (len < 1) {
      return '';
    }
    if (!this.canonicalize) {
      this.validateNameLength(len);
      return name;
    }
    var index = this.hashToIndex(h);
    var sym = this.symbols[index];
    if (sym !== null) {
      if (sym === name) {
        return sym;
      }
      for (var b = this.buckets[index >> 1]; b !== null; b = b.next) {
        if (b.symbol === name) {
          return b.symbol;
        }
      }
    }
    this.validateNameLength(len);
    return this.addSymbol(name, index);
  }

  validateNameLength(len) {
    if (len > this.maxNameLength) {
      throw new StreamConstraintsError('Name length exceeds the maximum allowed');
    }
  }

  addSymbol(name, index) {
    if (this.hashShared) {
      // copy-on-write of the shared parent arrays; the first insert never rehashes
      this.hashShared = false;
    } else if (this.size >= this.sizeThreshold) {
      this.rehash();
      index = this.hashToIndex(this.calcHash(name));
    }
    this.size++;
    if (this.symbols[index] === null) {
      this.symbols[index] = name;
    } else {
      var bix = index >> 1;
      var bucket = new Bucket(name, this.buckets[bix]);
      if (bucket.length > MAX_COLL_CHAIN_LENGTH) {
        this.handleSpillOverflow(bix, bucket, index);
      } else {
        this.buckets[bix] = bucket;
      }
    }
    return name;
  }

  handleSpillOverflow(bucketIndex, bucket, mainIndex) {
    if (this.overflows === null) {
      this.overflows = new Set();
      this.overflows.add(bucketIndex);
    } else if (this.overflows.has(bucketIndex)) {
      // FAIL_ON_SYMBOL_HASH_OVERFLOW is enabled by default
      throw new StreamConstraintsError('Longest collision chain in symbol table exceeds the maximum');
    } else {
      this.overflows.add(bucketIndex);
    }
    this.symbols[mainIndex] = bucket.symbol;
    this.buckets[bucketIndex] = null;
    this.size -= bucket.length;
  }

  rehash() {
    var size = this.symbols.length;
    var newSize = size + size;
    if (newSize > MAX_T_SIZE) {
      // past the maximum table size Jackson stops canonicalizing; the current name still goes into a fresh table
      this.size = 0;
      this.canonicalize = false;
      this.symbols = newArray(DEFAULT_T_SIZE);
      this.buckets = newArray(DEFAULT_T_SIZE >> 1);
      this.indexMask = DEFAULT_T_SIZE - 1;
      this.hashShared = false;
      return;
    }
    var oldSymbols = this.symbols;
    var oldBuckets = this.buckets;
    this.symbols = newArray(newSize);
    this.buckets = newArray(newSize >> 1);
    this.indexMask = newSize - 1;
    this.sizeThreshold = newSize - (newSize >> 2);
    var count = 0;
    var i;
    for (i = 0; i < size; i++) {
      if (oldSymbols[i] !== null) {
        count++;
        this.place(oldSymbols[i]);
      }
    }
    var bucketSize = size >> 1;
    for (i = 0; i < bucketSize; i++) {
      for (var b = oldBuckets[i]; b !== null; b = b.next) {
        count++;
        this.place(b.symbol);
      }
    }
    this.overflows = null;
    if (count !== this.size) {
      throw new Error('Internal error on SymbolTable.rehash()');
    }
  }

  place(symbol) {
    var index = this.hashToIndex(this.calcHash(symbol));
    if (this.symbols[index] === null) {
      this.symbols[index] = symbol;
    } else {
      var bix = index >> 1;
      this.buckets[bix] = new Bucket(symbol, this.buckets[bix]);
    }
  }
}

SymbolTable.StreamConstraintsError = StreamConstraintsError;
SymbolTable.hashName = hashName;
SymbolTable.randomSeed = randomSeed;
SymbolTable.HASH_MULT = HASH_MULT;
SymbolTable.DEFAULT_T_SIZE = DEFAULT_T_SIZE;
SymbolTable.MAX_T_SIZE = MAX_T_SIZE;
SymbolTable.MAX_COLL_CHAIN_LENGTH = MAX_COLL_CHAIN_LENGTH;
SymbolTable.DEFAULT_MAX_NAME_LEN = DEFAULT_MAX_NAME_LEN;

module.exports = SymbolTable;
