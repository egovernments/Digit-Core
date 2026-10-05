'use strict';

// Java 8 NFKC (java.text.Normalizer, Form.NFKC) on top of String.prototype.normalize. Code points the Java 8
// normalizer does not know (Character.isDefined false, plus the defined-but-not-normalized nfkcOpaque set) are left
// unchanged and split the input: each segment between them is normalized on its own. Lone surrogates are defined in
// Java and stay inside segments.

var chars = require('./javaChars');

function nfkcJ8(value) {
  var out = '';
  var segmentStart = 0;
  var n = value.length;
  for (var i = 0; i < n; i++) {
    var c = value.charCodeAt(i);
    if (c < 0x0370) {
      continue; // U+0000-U+036F are all defined in Java 8
    }
    var cp = c;
    var width = 1;
    if (c >= 0xD800 && c <= 0xDBFF && i + 1 < n) {
      var next = value.charCodeAt(i + 1);
      if (next >= 0xDC00 && next <= 0xDFFF) {
        cp = ((c - 0xD800) << 10) + (next - 0xDC00) + 0x10000;
        width = 2;
      }
    }
    if (!chars.isDefined(cp) || chars.isNfkcOpaque(cp)) {
      out += value.slice(segmentStart, i).normalize('NFKC') + value.slice(i, i + width);
      segmentStart = i + width;
    }
    i += width - 1;
  }
  if (segmentStart === 0) {
    return value.normalize('NFKC');
  }
  return out + value.slice(segmentStart).normalize('NFKC');
}

/** The start-up check required before NFKC is used: fullwidth less-than must normalize to '<'. */
function selfTest() {
  return nfkcJ8('\uFF1C') === '<';
}

module.exports = {
  nfkcJ8: nfkcJ8,
  selfTest: selfTest
};
