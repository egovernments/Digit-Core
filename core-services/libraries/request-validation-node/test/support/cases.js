'use strict';

// Deterministic probe generators. The expected results for these exact probes were produced once by the Java
// reference (InputStreamReader reads, jsoup 1.17.2, and the request-validation jar) and stored under test/fixtures.

var JavaRandom = require('./javaRandom');

function repeat(text, times) {
  var out = '';
  for (var i = 0; i < times; i++) {
    out += text;
  }
  return out;
}

function repeatBytes(bytes, times) {
  var out = [];
  for (var i = 0; i < times; i++) {
    for (var k = 0; k < bytes.length; k++) {
      out.push(bytes[k]);
    }
  }
  return out;
}

// ---------------------------------------------------------------------------------------------------------------
// Byte probes for utf8.readerView (4,000-unit reads, 8,192-byte decoder window).

var BAD = {
  lonecont: [0x80], c0: [0xC0, 0x80], c1: [0xC1, 0xBF], f5: [0xF5, 0x80, 0x80, 0x80], f8: [0xF8], ff: [0xFF],
  c2bad: [0xC2, 0x41], e0bad2: [0xE0, 0x80, 0x80], e1bad2: [0xE1, 0x41, 0x80], e1bad3: [0xE1, 0x80, 0x41],
  edsurr: [0xED, 0xA0, 0x80], f0bad2: [0xF0, 0x80, 0x80, 0x80], f4bad2: [0xF4, 0x90, 0x80, 0x80],
  f1bad3: [0xF1, 0x80, 0x41, 0x80], f1bad4: [0xF1, 0x80, 0x80, 0x41]
};
var TRUNC = { c2end: [0xC2], e1end: [0xE1], e1x80end: [0xE1, 0x80], f1end: [0xF1], f1x80end: [0xF1, 0x80],
  f1x8080end: [0xF1, 0x80, 0x80] };
var GOOD = { a: [0x41], two: [0xC3, 0xA9], three: [0xE0, 0xA4, 0x95], four: [0xF0, 0x9F, 0x99, 0x82] };
var BOM = [0xEF, 0xBB, 0xBF];

function fill(units, kind) {
  return repeatBytes(GOOD[kind], kind === 'four' ? units / 2 : units);
}

function readerCases() {
  var cases = [];
  var push = function (bytes) {
    cases.push(Buffer.from(bytes));
  };
  ['a', 'two', 'three', 'four'].forEach(function (kind) {
    [3990, 3998, 3999, 4000, 4001, 4002, 4010, 7990, 7999, 8000, 8001, 11999, 12000, 12001].forEach(function (at) {
      var pre = fill(kind === 'four' ? at - (at % 2) : at, kind);
      Object.keys(BAD).forEach(function (k) {
        push(pre.concat(BAD[k], [0x41, 0x42]));
      });
      Object.keys(TRUNC).forEach(function (k) {
        push(pre.concat(TRUNC[k]));
      });
      push(pre.concat(GOOD.four, [0x41]));
      if (kind === 'a') {
        push(BOM.concat(pre, BAD.e1bad2));
        push(BOM.concat(pre, TRUNC.f1x80end));
      }
    });
  });
  // A full 4,000-unit read ending at the end of the decoder's 8,192-byte window, then a lead whose deciding bytes
  // are outside it (191 three-byte + 3,809 two-byte characters = 8,191 bytes), with neighbours.
  [[191, 3809], [190, 3810], [192, 3808], [191, 3808], [191, 3810], [383, 7617], [382, 7618]].forEach(function (mix) {
    var pre = fill(mix[0], 'three').concat(fill(mix[1], 'two'));
    Object.keys(BAD).forEach(function (k) {
      push(pre.concat(BAD[k], [0x41]));
      push(BOM.concat(pre, BAD[k], [0x41]));
    });
    Object.keys(TRUNC).forEach(function (k) {
      push(pre.concat(TRUNC[k]));
    });
    push(pre.concat(GOOD.four, GOOD.three, [0x41]));
  });
  [8185, 8189, 8190, 8191, 8192, 8193, 16383, 16384].forEach(function (pos) {
    Object.keys(BAD).forEach(function (k) {
      push(repeatBytes([0x61], pos).concat(BAD[k], [0x41]));
    });
  });
  var random = new JavaRandom(4242);
  var badKeys = Object.keys(BAD);
  var truncKeys = Object.keys(TRUNC);
  for (var n = 0; n < 1500; n++) {
    var length = n < 300 ? random.nextInt(64) + 1 : random.nextInt(20000) + 1;
    var bytes = [];
    while (bytes.length < length) {
      var r = random.nextInt(10000);
      var seq;
      if (r < 4) {
        seq = BAD[badKeys[random.nextInt(badKeys.length)]];
      } else if (r < 5000) {
        seq = [0x20 + random.nextInt(0x5F)];
      } else if (r < 7500) {
        seq = GOOD.two;
      } else if (r < 9500) {
        seq = GOOD.three;
      } else {
        seq = GOOD.four;
      }
      for (var s = 0; s < seq.length; s++) {
        bytes.push(seq[s]);
      }
    }
    if (random.nextInt(10) === 0) {
      bytes = bytes.concat(TRUNC[truncKeys[random.nextInt(truncKeys.length)]]);
    }
    if (random.nextInt(20) === 0) {
      bytes = BOM.concat(bytes);
    }
    push(bytes);
  }
  return cases;
}

// ---------------------------------------------------------------------------------------------------------------
// Long inputs for the jsoup CharacterReader window (32,768 units; split point 24,576; mark read-ahead 1,024).

function jsoupLongCases() {
  var cases = [];
  [0, 1023, 1024, 24575, 24576, 24577, 31743, 31744, 32767, 32768, 32769, 49151, 49152, 57343, 57344, 65536]
      .forEach(function (prefix) {
        [1022, 1023, 1024, 32767].forEach(function (run) {
          var p = repeat('a', prefix);
          cases.push(p + ' &#' + repeat('0', run) + '106;avascript:alert(1)');
          cases.push(p + ' &#x' + repeat('0', run) + '3C;b>');
          cases.push(p + ' &#' + repeat('9', run) + ';x');
          cases.push(p + ' &' + repeat('b', run) + ';x');
          cases.push(p + ' &amp' + repeat('1', run) + ';x');
        });
      });
  var random = new JavaRandom(777);
  var atoms = ['&', '&#', '&#x', '&#X', ';', 'amp', 'lt', 'gt', 'quot', 'nbsp', 'not', 'notin', 'AMP', 'x', 'X', '1',
    '0', '9', 'A', 'f', ' ', '<', '\t', '\u00e9', '\u0928', '\u00ff', 'ab', '&amp;', '&lt;', '&#60;', '&#x3c',
    '&#128;', '&#x110000;', '&#55296;', '\uD83D\uDE42'];
  for (var n = 0; n < 60; n++) {
    var target = [24576, 32768, 49152, 65536][n % 4] + random.nextInt(4000) - 2000;
    var s = '';
    while (s.length < target) {
      var r = random.nextInt(100);
      if (r < 8) {
        s += '&#' + repeat('0', random.nextInt(1500)) + (random.nextInt(2) ? '60;' : '1');
      } else if (r < 12) {
        s += '&' + repeat('a', random.nextInt(1300));
      } else {
        s += atoms[random.nextInt(atoms.length)];
      }
    }
    cases.push(s);
  }
  return cases;
}

// ---------------------------------------------------------------------------------------------------------------
// Long detection probes (expanded from compact specs; the fixture stores the spec, not the value).

/** spec: a string, or an array of [text, times] pairs and strings, concatenated. */
function expand(spec) {
  if (typeof spec === 'string') {
    return spec;
  }
  return spec.map(function (part) {
    return typeof part === 'string' ? part : repeat(part[0], part[1]);
  }).join('');
}

module.exports = {
  readerCases: readerCases,
  jsoupLongCases: jsoupLongCases,
  expand: expand,
  repeat: repeat
};
