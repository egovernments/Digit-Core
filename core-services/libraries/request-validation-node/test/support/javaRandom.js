'use strict';

// java.util.Random (48-bit linear congruential generator) so tests can replay the Java tests' seeded inputs.
// The 48-bit state is kept as two 24-bit halves; every intermediate value stays below 2^53.

var TWO24 = 16777216;
var MUL_HI = 0x5DE;      // 0x5DEECE66D >>> 24
var MUL_LO = 0xECE66D;   // 0x5DEECE66D & 0xFFFFFF

function JavaRandom(seed) {
  // (seed ^ 0x5DEECE66D) & ((1 << 48) - 1), for a seed that fits in 32 bits
  if (!Number.isInteger(seed) || seed < 0 || seed > 0x7FFFFFFF) {
    throw new RangeError('seed must be a non-negative 32-bit integer');
  }
  var lo = (seed % TWO24) ^ MUL_LO;
  var hi = Math.floor(seed / TWO24) ^ MUL_HI;
  this.lo = lo;
  this.hi = hi;
}

JavaRandom.prototype.next = function (bits) {
  var product = this.lo * MUL_LO + 0xB;
  var carry = Math.floor(product / TWO24);
  var lo = product % TWO24;
  var hi = (this.hi * MUL_LO + this.lo * MUL_HI + carry) % TWO24;
  this.lo = lo;
  this.hi = hi;
  // (int) (seed >>> (48 - bits)) for bits <= 31
  return Math.floor((hi * TWO24 + lo) / Math.pow(2, 48 - bits));
};

JavaRandom.prototype.nextInt = function (bound) {
  if (!(bound > 0)) {
    throw new RangeError('bound must be positive');
  }
  var r = this.next(31);
  var m = bound - 1;
  if ((bound & m) === 0) {
    return Math.floor(r * bound / 2147483648);
  }
  for (var u = r; ((u - (r = u % bound) + m) | 0) < 0; u = this.next(31)) {
    // retry, as Java does when u - r + m overflows int
  }
  return r;
};

module.exports = JavaRandom;
