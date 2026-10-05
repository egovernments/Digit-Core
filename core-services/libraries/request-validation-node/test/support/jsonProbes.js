'use strict';

// Deterministic JSON byte probes for the inspector. Every generator returns rows
// { id, body: Buffer, limits: 'csv' | '-', flags: 'nodup,nodual,skip=/x' | '' }; tsv(rows) is the exact text whose
// SHA-256 the fixtures record, so a test can prove it regenerated the inputs the reference run saw.
//  - targeted(): number length paths, \u escapes, segmented text-buffer checks, read boundaries of valid input,
//    malformed UTF-8 near read boundaries, symbol-table floods, nesting, tokens next to a failing read;
//  - fuzz(seed, count): random documents, random limits and flags;
//  - limitBoundaries(): every inspection limit at exactly the limit and one past it.

var DEFAULT_LIMITS = [10485760, 64, 1000000, 256, 2000000, 1000, 65536];
var FIELD_INDEX = {
  maxBodyBytes: 0, maxDepth: 1, maxStringLength: 2, maxNameLength: 3, maxTokens: 4, maxNumberLength: 5,
  maxScalarLength: 6
};

function limitsCsv(overrides) {
  var values = DEFAULT_LIMITS.slice();
  Object.keys(overrides || {}).forEach(function (key) {
    values[FIELD_INDEX[key]] = overrides[key];
  });
  return values.join(',');
}

function row(id, body, limits, flags) {
  return { id: id, body: Buffer.isBuffer(body) ? body : Buffer.from(body, 'utf8'), limits: limits || '-', flags: flags || '' };
}

function tsv(rows) {
  return rows.map(function (r) {
    return [r.id, r.body.toString('hex'), r.limits, r.flags].join('\t');
  }).join('\n') + '\n';
}

/** { maxBodyBytes, ..., rejectDuplicateKeys, rejectDualRequestInfo, skipPaths } of a row. */
function config(r) {
  var c = {};
  if (r.limits !== '-') {
    var v = r.limits.split(',').map(Number);
    Object.keys(FIELD_INDEX).forEach(function (key) {
      c[key] = v[FIELD_INDEX[key]];
    });
  }
  c.rejectDuplicateKeys = true;
  c.rejectDualRequestInfo = true;
  c.skipPaths = [];
  r.flags.split(',').forEach(function (flag) {
    if (flag === 'nodup') {
      c.rejectDuplicateKeys = false;
    } else if (flag === 'nodual') {
      c.rejectDualRequestInfo = false;
    } else if (flag.indexOf('skip=') === 0) {
      c.skipPaths.push(flag.slice(5));
    }
  });
  return c;
}

function digitCount(text) {
  var n = 0;
  for (var i = 0; i < text.length; i++) {
    if (text.charCodeAt(i) >= 0x30 && text.charCodeAt(i) <= 0x39) {
      n++;
    }
  }
  return n;
}

function pad(prefixLen, target, ch) {
  return (ch || 'a').repeat(Math.max(0, target - prefixLen));
}

function targetedNumbersEscapesBuffers(rows) {
  var L = limitsCsv;
  // A. number lengths on Jackson's fast and slow paths
  var nums = [];
  ['', '-'].forEach(function (sign) {
    ['0', '1', '7'].forEach(function (lead) {
      [0, 1, 3].forEach(function (moreInt) {
        if (lead === '0' && moreInt) {
          return;
        }
        var intPart = lead + '5'.repeat(moreInt);
        ['', '.1', '.123', '.0001'].forEach(function (fr) {
          ['', 'e5', 'E+12', 'e-123', 'e0001'].forEach(function (ex) {
            nums.push(sign + intPart + fr + ex);
          });
        });
      });
    });
  });
  nums.forEach(function (n, i) {
    var digits = digitCount(n);
    [digits - 2, digits - 1, digits, digits + 1].forEach(function (mnl) {
      if (mnl < 1) {
        return;
      }
      var lim = L({ maxNumberLength: mnl });
      rows.push(row('A-arr-' + i + '-' + mnl, '[' + n + ']', lim));
      rows.push(row('A-obj-' + i + '-' + mnl, '{"n":' + n + '}', lim));
      rows.push(row('A-root-' + i + '-' + mnl, n, lim));
      rows.push(row('A-rootsp-' + i + '-' + mnl, n + ' ', lim));
    });
  });

  // B. \u escapes: Jackson takes the low byte of each unit as the hex digit
  var units = ['0', '3', 'C', 'c', 'f', 'g', 'G', ' ', '\u0130', '\u0133', '\u0139', '\u0141', '\u0146', '\u0147',
    '\u0161', '\u0166', '\u0230', '\u4E43', '\uFF10', '\uFF41', '\u0100', '\u00E0', '\u01E0', '\uD83D\uDC30'];
  var bi = 0;
  for (var x = 0; x < units.length; x++) {
    for (var y = 0; y < units.length; y++) {
      var esc = '\\u' + units[x] + units[y] + '3C';
      rows.push(row('B-val-' + (bi++), '{"a":"' + esc + 'b>"}'));
      rows.push(row('B-name-' + (bi++), '{"' + units[y] + esc + 'i>":1}'));
    }
  }
  ['\u0130\u01303C', '0\u0130\u0133\u0143', '\uFF10\uFF10\uFF13\uFF23', '\u0230\u0230\u0233\u0243'].forEach(function (u, i) {
    rows.push(row('B-script-' + i, '{"a":"\\u' + u + 'script>"}'));
    rows.push(row('B-scriptname-' + i, '{"\\u' + u + 'script>":"x"}'));
  });

  // C. segmented text-buffer checks (fresh state: first segment 200, then 500, 750, 1125, ...)
  [150, 199, 200, 201, 250, 500, 699, 700, 701, 1000, 1449, 1450, 1451].forEach(function (msl) {
    [190, 199, 200, 201, 202, 650, 699, 700, 701, 1449, 1450, 1451, 1452, 2000].forEach(function (n) {
      var plain = 'x'.repeat(n);
      rows.push(row('C1-' + msl + '-' + n, '{"a":"\\n' + plain + '\\q"}', L({ maxStringLength: msl })));
      rows.push(row('C2-' + msl + '-' + n, '{"a":"\\n' + plain + '\t"}', L({ maxStringLength: msl })));
      rows.push(row('C3-' + msl + '-' + n, '{"a":"\\n' + plain, L({ maxStringLength: msl })));
      rows.push(row('C4-' + msl + '-' + n, '{"\\n' + plain + '":1}', L({ maxStringLength: msl, maxNameLength: 100000 })));
      rows.push(row('C4b-' + msl + '-' + n, '{"' + plain + '\\n":1}', L({ maxStringLength: msl, maxNameLength: 100000 })));
      rows.push(row('C5-' + msl + '-' + n, '[0.' + '1'.repeat(n) + ']', L({ maxStringLength: msl, maxNumberLength: 100000 })));
      rows.push(row('C5b-' + msl + '-' + n, '{"n":-0.' + '2'.repeat(n) + 'e5}',
          L({ maxStringLength: msl, maxNumberLength: 100000 })));
      rows.push(row('C6-' + msl + '-' + n, '["\\n' + 'y'.repeat(Math.max(0, msl - 1)) + '","\\n' + plain + '\\q"]',
          L({ maxStringLength: msl })));
    });
  });
  [100, 300, 3000].forEach(function (msl) {
    [150, 250, 600, 3900].forEach(function (p) {
      rows.push(row('C7-' + msl + '-' + p, '{"a":"' + 'z'.repeat(p) + '\\n' + 'w'.repeat(500) + '\\q"}',
          L({ maxStringLength: msl })));
    });
  });

  // D. valid input: tokens across read ends (4,000 units; 3,999 when a surrogate pair straddles)
  [3990, 3995, 3997, 3998, 3999, 4000, 4001, 4003, 7995, 7998, 7999, 8000, 8001].forEach(function (at) {
    ['1.234', '1e234', '12.5e1', '-1.25', '0.123', '123'].forEach(function (num, ni) {
      var head = '["';
      var doc = head + pad(head.length + 2, at, 'a') + '",' + num + ']';
      var digits = digitCount(num);
      [digits - 1, digits].forEach(function (mnl) {
        rows.push(row('D1-' + at + '-' + ni + '-' + mnl, doc, L({ maxNumberLength: mnl })));
      });
      var emojiHead = '["' + 'b'.repeat(3999 - 2) + '\uD83D\uDE00' + '",';
      var doc2 = emojiHead + '"' + pad(emojiHead.length + 1 + 2, at, 'c') + '",' + num + ']';
      [digits - 1, digits].forEach(function (mnl) {
        rows.push(row('D2-' + at + '-' + ni + '-' + mnl, doc2, L({ maxNumberLength: mnl })));
      });
    });
    rows.push(row('D3s-' + at, '{"a":"' + pad(6, at - 50) + 'q'.repeat(120) + '"}', L({ maxStringLength: 100 })));
    rows.push(row('D3n-' + at, '{"' + pad(2, at - 50) + 'q'.repeat(120) + '":1}',
        L({ maxStringLength: 100, maxNameLength: 100000 })));
    rows.push(row('D3n2-' + at, '[{"k":1},{"' + pad(12, at - 150) + 'r'.repeat(300) + '":1}]',
        L({ maxStringLength: 250, maxNameLength: 100000 })));
    ['true', 'false', 'null'].forEach(function (lit) {
      var h = '["' + pad(2 + 2, at - lit.length) + '",';
      rows.push(row('D4-' + at + '-' + lit, h + lit + 'x]'));
      rows.push(row('D4b-' + at + '-' + lit, h + lit + ']'));
    });
  });

  // E. malformed UTF-8 at decoded offsets around read ends
  var bad = {
    ff: [0xFF], c3x: [0xC3, 0x28], e2x: [0xE2, 0x28, 0xA1], e0bad: [0xE0, 0x80, 0x80], f0bad: [0xF0, 0x80, 0x80, 0x80],
    f5: [0xF5, 0x80, 0x80, 0x80], trunc2: [0xC3], trunc3: [0xE2, 0x82], trunc4: [0xF0, 0x9F, 0x98], cont: [0x80],
    surr: [0xED, 0xA0, 0x80]
  };
  Object.keys(bad).forEach(function (k) {
    for (var off = 3990; off <= 4010; off += 2) {
      var pre = Buffer.from('{"a":"<b>","b":"' + 'd'.repeat(off - 16), 'utf8');
      rows.push(row('E-' + k + '-' + off, Buffer.concat([pre, Buffer.from(bad[k]), Buffer.from('"}')])));
    }
    for (var off2 = 7990; off2 <= 8010; off2 += 4) {
      var pre2 = Buffer.from('{"a":"<b>","b":"' + '\u00E9'.repeat(off2 - 16), 'utf8');
      rows.push(row('E2-' + k + '-' + off2, Buffer.concat([pre2, Buffer.from(bad[k]), Buffer.from('"}')])));
    }
  });
}

// Fully colliding names (equal length, equal seedless hash): the blocks "aA" and "b " both fold to 33 * 97 + 65.
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

function objectOf(names) {
  return '{' + names.map(function (n) {
    return JSON.stringify(n) + ':1';
  }).join(',') + '}';
}

function ordinary(n, prefix) {
  var out = [];
  for (var i = 0; i < n; i++) {
    out.push((prefix || 'o') + i);
  }
  return out;
}

function targetedSymbolsNestingReads(rows) {
  var C = colliding(1024, 10);
  rows.push(row('S-reset', objectOf(C.slice(0, 152).concat(ordinary(191), C.slice(152, 152 + 151)))));
  rows.push(row('S-reset-minus', objectOf(C.slice(0, 152).concat(ordinary(150), C.slice(152, 152 + 151)))));
  rows.push(row('S-noreset-303', objectOf(C.slice(0, 303))));
  rows.push(row('S-302', objectOf(C.slice(0, 302))));
  var first = objectOf(C.slice(0, 152));
  rows.push(row('S-repeat', first.slice(0, first.length - 1) + ',' + C.slice(0, 160).map(function (n) {
    return JSON.stringify(n) + ':2';
  }).join(',') + '}', null, 'nodup'));
  rows.push(row('S-spread-repeat', '[' + C.slice(0, 160).map(function (n) {
    return objectOf([n, 'x', 'y']);
  }).join(',') + ',' + C.slice(0, 160).map(function (n) {
    return objectOf([n]);
  }).join(',') + ']'));
  rows.push(row('S-cutoff', '[' + objectOf(ordinary(49153, 'u')) + ',' + objectOf(C.slice(0, 400)) + ']', limitsCsv({})));
  rows.push(row('S-cutoff-minus', '[' + objectOf(ordinary(49100, 'u')) + ',' + objectOf(C.slice(0, 400)) + ']', limitsCsv({})));
  rows.push(row('S-cutoff-long', '[' + objectOf(ordinary(49153, 'u')) + ',{"' + 'L'.repeat(50001) + '":1}]',
      limitsCsv({ maxNameLength: 100000 })));
  [64, 65, 255, 256, 257].forEach(function (d) {
    var deep = limitsCsv({ maxDepth: 256 });
    rows.push(row('N-arr-' + d, '['.repeat(d) + ']'.repeat(d), deep));
    rows.push(row('N-obj-' + d, '{"a":'.repeat(d - 1) + '{}' + '}'.repeat(d - 1), deep));
    rows.push(row('N-def-' + d, '['.repeat(d) + ']'.repeat(d)));
  });
  var toks = [['num', '12345'], ['flt', '1.5e3'], ['lit', 'true'], ['nul', 'null'], ['str', '"s"'], ['obj', '{}']];
  for (var at = 3994; at <= 4002; at++) {
    toks.forEach(function (t) {
      var head = '{"a":"<b>","p":"';
      var padding = 'p'.repeat(Math.max(0, at - head.length - 2 - t[1].length - 4));
      var pre = head + padding + '","v":' + t[1];
      rows.push(row('R-' + t[0] + '-' + at, Buffer.concat([Buffer.from(pre), Buffer.from([0xFF]), Buffer.from('}')])));
      rows.push(row('R2-' + t[0] + '-' + at, Buffer.concat([Buffer.from(pre + ','), Buffer.from([0xFF]), Buffer.from('}')])));
      var preN = head + padding + '","nnnn"';
      rows.push(row('R3-' + t[0] + '-' + at, Buffer.concat([Buffer.from(preN), Buffer.from([0xC3, 0x28]), Buffer.from(':1}')])));
    });
  }
}

function targeted() {
  var rows = [];
  targetedNumbersEscapesBuffers(rows);
  targetedSymbolsNestingReads(rows);
  return rows;
}

/** Random documents: seeded, so the same seed and count always give the same rows. */
function fuzz(seedValue, count) {
  var seed = seedValue >>> 0;
  function rnd() {
    seed = (seed + 0x6D2B79F5) >>> 0;
    var t = seed;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  }
  function ri(n) {
    return Math.floor(rnd() * n);
  }
  function pick(a) {
    return a[ri(a.length)];
  }
  var STR_PARTS = ['a', 'bc', 'Hello', ' ', '<b>', '<', 'javascript:', 'java\\tscript:', 'onload=', 'on', '=',
    '&lt;b&gt;', '&#60;i', '%3Cb', '%253C', 'data:text/html,', '\\n', '\\"', '\\\\', '\\/', '\\u003C', '\\u003c',
    '\\uD83D\\uDE00', '\\uD800', '\\uDC00x', '\\u0000', '\\u\u0130\u01303C', '\\u0\u01333c', '\\q', '\\u12', '\\u12G4',
    '\t', '\u0000', '\u0001', '\u007f', '\u00E9', '\u0905', '\uD83D\uDE00', '\u2028', '\u00A0', '\uFEFF', '\u180E',
    '\uFE70', '\uFF1C', 'x'.repeat(50), '\\b\\f\\r\\t'];
  var NAME_PARTS = ['a', 'b', 'x', 'RequestInfo', 'requestInfo', 'id', '', '<k>', 'onclick=', '\\u0061', '\\n', '\u00E9',
    '\uD83D\uDE00', ' ', 'n'.repeat(30), '\\q'];
  var NUMS = ['0', '1', '-1', '12', '123456', '0.5', '-0.5', '1.25', '1e5', '1E+5', '1e-5', '-0', '0e0', '0.0001',
    '12.5e10', '01', '1.', '.5', '+1', '1e', '-', '--1', '1.e5', '0x1F', '1e+', '00', '-01', '9'.repeat(12),
    '1.' + '2'.repeat(9), '0.' + '3'.repeat(9)];
  var LITS = ['true', 'false', 'null', 'tru', 'nul', 'truex', 'true1', 'null_', 'false$', 'true\u00E9', 'true\u20AC',
    'true<', 'null\u0100', 'True', 'NaN', 'Infinity', '-Infinity'];
  var WS = [' ', ' ', ' ', '\n', '\t', '\r', '\r\n', '', '', '', '\u000b', '\u000c', '\u00A0'];
  function ws() {
    return rnd() < 0.7 ? '' : pick(WS);
  }
  function str(parts, max) {
    var s = '';
    var n = ri(max);
    for (var i = 0; i < n; i++) {
      s += pick(parts);
    }
    return s;
  }
  function value(d) {
    var r = rnd();
    if (d > 4 || r < 0.35) {
      var k = rnd();
      if (k < 0.55) {
        return '"' + str(STR_PARTS, 4) + '"';
      }
      if (k < 0.8) {
        return pick(NUMS);
      }
      return pick(LITS);
    }
    if (r < 0.7) {
      var n = ri(5);
      var out = '{' + ws();
      for (var i = 0; i < n; i++) {
        out += (i ? ',' + ws() : '') + '"' + str(NAME_PARTS, 3) + '"' + ws() + ':' + ws() + value(d + 1) + ws();
      }
      return out + '}';
    }
    var m = ri(5);
    var a = '[' + ws();
    for (var j = 0; j < m; j++) {
      a += (j ? ',' + ws() : '') + value(d + 1) + ws();
    }
    return a + ']';
  }
  var rows = [];
  for (var c = 0; c < count; c++) {
    var doc = ws() + value(0) + ws();
    var r = rnd();
    if (r < 0.06) {
      doc += pick(['{}', ' x', ',', ']', '}', ' 5', ' "s"', ' true', '5', '[1]']);
    } else if (r < 0.12 && doc.length > 1) {
      doc = doc.slice(0, ri(doc.length));
    } else if (r < 0.2 && doc.length > 1) {
      var p = ri(doc.length);
      doc = doc.slice(0, p) + pick(['"', ',', ':', '{', ']', '\\', '0', 'e', '-', '.', ' ', 'x', '\u0000']) + doc.slice(p + 1);
    }
    if (rnd() < 0.08) {
      var target = pick([3990, 3995, 3998, 3999, 4000, 4001, 4005, 7995, 7999, 8000, 8003]) + ri(8) - 4;
      var fill = pick(['p', '\u00E9', '\uD83D\uDE00', '\u0905']);
      var padding = '';
      while (padding.length < target) {
        padding += fill;
      }
      doc = '["' + padding + '",' + doc + ']';
    }
    var bytes = Buffer.from(doc, 'utf8');
    if (rnd() < 0.08) {
      var pos = ri(bytes.length + 1);
      var junk = pick([[0xFF], [0xC3], [0xE2, 0x82], [0xED, 0xA0, 0x80], [0x80], [0xF0, 0x9F], [0xC0, 0xAF],
        [0xF4, 0x90, 0x80, 0x80]]);
      bytes = Buffer.concat([bytes.slice(0, pos), Buffer.from(junk), bytes.slice(pos)]);
    }
    if (rnd() < 0.05) {
      bytes = Buffer.concat([Buffer.from([0xEF, 0xBB, 0xBF]), rnd() < 0.3 ? Buffer.from([0xEF, 0xBB, 0xBF]) : Buffer.alloc(0),
        bytes]);
    }
    var lim = DEFAULT_LIMITS.slice();
    if (rnd() < 0.6) {
      if (rnd() < 0.3) {
        lim[0] = Math.max(1, bytes.length + ri(5) - 2);
      }
      if (rnd() < 0.3) {
        lim[1] = 1 + ri(6);
      }
      if (rnd() < 0.3) {
        lim[2] = pick([1 + ri(12), 1 + ri(300), 3990 + ri(20)]);
      }
      if (rnd() < 0.3) {
        lim[3] = 1 + ri(40);
      }
      if (rnd() < 0.3) {
        lim[4] = 1 + ri(60);
      }
      if (rnd() < 0.4) {
        lim[5] = 1 + ri(14);
      }
    }
    var flags = [];
    if (rnd() < 0.1) {
      flags.push('nodup');
    }
    if (rnd() < 0.1) {
      flags.push('nodual');
    }
    if (rnd() < 0.1) {
      flags.push('skip=' + pick(['/a', '/*', '/0', '/*/*', '/x/0', '/', '/RequestInfo']));
    }
    rows.push(row('F' + c, bytes, lim.join(','), flags.join(',')));
  }
  return rows;
}

/** Each limit at exactly the limit (accepted) and one past it (rejected); maxScalarLength never applies to JSON. */
function limitBoundaries() {
  var rows = [];
  var L = limitsCsv;
  var body = '{"abc":"xyz","n":[-12.5,{"k":true}]}';
  var size = Buffer.byteLength(body);
  rows.push(row('maxBodyBytes-exact', body, L({ maxBodyBytes: size })));
  rows.push(row('maxBodyBytes-plus1', body, L({ maxBodyBytes: size - 1 })));
  rows.push(row('maxDepth-exact', '[[{"a":[]}]]', L({ maxDepth: 4 })));
  rows.push(row('maxDepth-plus1', '[[{"a":[]}]]', L({ maxDepth: 3 })));
  rows.push(row('maxDepth-plus1-value', '{"a":{"b":[1]}}', L({ maxDepth: 2 })));
  rows.push(row('maxStringLength-exact', '{"s":"abcde"}', L({ maxStringLength: 5 })));
  rows.push(row('maxStringLength-plus1', '{"s":"abcdef"}', L({ maxStringLength: 5 })));
  rows.push(row('maxStringLength-escaped-exact', '{"s":"a\\u0062\\n\\"e"}', L({ maxStringLength: 5 })));
  rows.push(row('maxStringLength-escaped-plus1', '{"s":"a\\u0062\\n\\"ef"}', L({ maxStringLength: 5 })));
  rows.push(row('maxNameLength-exact', '{"abcde":1}', L({ maxNameLength: 5 })));
  rows.push(row('maxNameLength-plus1', '{"abcdef":1}', L({ maxNameLength: 5 })));
  rows.push(row('maxTokens-exact', '{"a":[1,"x"]}', L({ maxTokens: 7 })));
  rows.push(row('maxTokens-plus1', '{"a":[1,"x"]}', L({ maxTokens: 6 })));
  rows.push(row('maxNumberLength-int-exact', '[12345]', L({ maxNumberLength: 5 })));
  rows.push(row('maxNumberLength-int-plus1', '[123456]', L({ maxNumberLength: 5 })));
  rows.push(row('maxNumberLength-neg-text-exact', '[-1234]', L({ maxNumberLength: 5 })));
  rows.push(row('maxNumberLength-neg-text-plus1', '[-12345]', L({ maxNumberLength: 5 })));
  rows.push(row('maxNumberLength-float-exact', '[1.2345]', L({ maxNumberLength: 6 })));
  rows.push(row('maxNumberLength-float-plus1', '[1.23456]', L({ maxNumberLength: 6 })));
  rows.push(row('maxNumberLength-zero-float-plus1', '[0.12345]', L({ maxNumberLength: 6 })));
  rows.push(row('maxScalarLength-irrelevant', '{"' + 'k'.repeat(10) + '":"' + 'v'.repeat(100) + '"}', L({ maxScalarLength: 1 })));
  rows.push(row('jacksonName-50000', '{"' + 'n'.repeat(50000) + '":1}', L({ maxNameLength: 100000 })));
  rows.push(row('jacksonName-50001', '{"' + 'n'.repeat(50001) + '":1}', L({ maxNameLength: 100000 })));
  rows.push(row('jacksonName-50001-libraryLimitLarger', '{"a":1,"' + 'n'.repeat(50001) + '":1}', L({ maxNameLength: 2000000 })));
  return rows;
}

module.exports = {
  DEFAULT_LIMITS: DEFAULT_LIMITS,
  limitsCsv: limitsCsv,
  tsv: tsv,
  config: config,
  colliding: colliding,
  targeted: targeted,
  fuzz: fuzz,
  limitBoundaries: limitBoundaries
};
