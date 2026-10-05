'use strict';

// Symbol-table scenarios: [seed, names[]] pairs replayed against the name canonicalizer. The names that need a
// seed-specific search (partial collisions, sibling buckets) come from the fixture; everything else is generated.

function colliding(n, k) {
  var out = [];
  for (var i = 0; out.length < n; i++) {
    var s = '';
    for (var b = 0; b < k; b++) {
      s += ((i >> b) & 1) ? 'b ' : 'aA';
    }
    out.push(s);
  }
  return out;
}

function range(prefix, n) {
  var out = [];
  for (var i = 0; i < n; i++) {
    out.push(prefix + i);
  }
  return out;
}

/** The inverse of 33 modulo 2^32 (33 is odd), by Newton iteration. */
function inverse33() {
  var inv = 33;
  for (var r = 0; r < 6; r++) {
    inv = Math.imul(inv, (2 - Math.imul(33, inv)) | 0);
  }
  return inv;
}

/**
 * @param {{partialSeed: number, partial: string[], siblingSeed: number, siblingA: string[], siblingB: string[]}} fixed
 */
function scenarios(fixed) {
  var state = 12345;
  function rnd() {
    state = (state + 0x6D2B79F5) >>> 0;
    var t = state;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  }
  function ri(n) {
    return Math.floor(rnd() * n);
  }
  var out = [];
  // random short names with many repeats
  for (var s = 0; s < 6; s++) {
    var names = [];
    for (var i = 0; i < 3000; i++) {
      var len = ri(6);
      var n = '';
      for (var k = 0; k < len; k++) {
        n += String.fromCharCode(97 + ri(6));
      }
      names.push(n);
    }
    out.push([(ri(0x7FFFFFFF) + 1) | 0, names]);
  }
  // distinct names through every table size up to 65,536 and past the canonicalization cut-off
  var many = [];
  for (var j = 0; j < 52000; j++) {
    many.push('n' + j.toString(36) + 'x' + ri(1000));
  }
  for (var j2 = 0; j2 < 2000; j2++) {
    many.push(many[ri(many.length)]);
  }
  many.push('L'.repeat(50000));
  many.push('L'.repeat(50001));
  out.push([987654321, many]);
  // fully colliding floods
  var C = colliding(1024, 10);
  out.push([7, C.slice(0, 152).concat(['o1', 'o2'], C.slice(152, 400))]);
  out.push([99, C.slice(0, 152).concat(range('p', 191), C.slice(152, 400))]);
  out.push([-5, C.slice(0, 160).concat(C.slice(0, 160))]);
  out.push([123, C.slice(0, 1000)]);
  // seed-specific partial collisions
  out.push([fixed.partialSeed, fixed.partial.concat(['r1', 'r2'], fixed.partial.slice(0, 50))]);
  // a name whose parser hash is 0 (calcHash makes it 1, so after a rehash it is not found where it is looked up)
  var zeroSeed = Math.imul(-97, inverse33()) | 0;
  out.push([zeroSeed, ['a'].concat(range('z', 60), ['a', 'a', 'b', 'a'])]);
  // sibling indexes sharing one bucket
  var A = fixed.siblingA;
  var B = fixed.siblingB;
  var fill = range('fill', 45);
  out.push([fixed.siblingSeed, B.slice(0, 5).concat(A.slice(0, 5), fill, A.slice(5, 170), B.slice(0, 5), A.slice(0, 5))]);
  out.push([fixed.siblingSeed, A.slice(0, 5).concat(B.slice(0, 5), fill, A.slice(5, 170), B.slice(0, 5), A.slice(0, 5))]);
  return out;
}

/** The replay input format: "seed <int>" lines, then each name as hex UTF-16 units joined by "." ("-" = empty). */
function encode(list) {
  var lines = [];
  list.forEach(function (sc) {
    lines.push('seed ' + sc[0]);
    sc[1].forEach(function (name) {
      if (name === '') {
        lines.push('-');
        return;
      }
      var units = [];
      for (var i = 0; i < name.length; i++) {
        units.push(name.charCodeAt(i).toString(16));
      }
      lines.push(units.join('.'));
    });
  });
  return lines.join('\n') + '\n';
}

module.exports = { scenarios: scenarios, encode: encode, colliding: colliding };
