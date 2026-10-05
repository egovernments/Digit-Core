'use strict';

// Port of EventHandlerNameList: loads data/event-handler-names.txt (byte-identical to the Java resource) with the
// Java parser's rules. Lines end at LF, CR or CRLF; each line is trimmed (String.trim) and lower-cased
// (Locale.ROOT); blank lines and lines starting with '#' are skipped; every other line must match on[a-z0-9]+;
// an empty result is an error.

var fs = require('fs');
var path = require('path');
var chars = require('./jdk/javaChars');
var utf8 = require('./jdk/utf8');

var RESOURCE = path.join(__dirname, '..', '..', 'data', 'event-handler-names.txt');

// on[a-z0-9]+ by character scan.
function isHandlerName(value) {
  if (value.length < 3 || value.charCodeAt(0) !== 0x6F || value.charCodeAt(1) !== 0x6E) {
    return false;
  }
  for (var i = 2; i < value.length; i++) {
    var c = value.charCodeAt(i);
    if (!((c >= 0x61 && c <= 0x7A) || (c >= 0x30 && c <= 0x39))) {
      return false;
    }
  }
  return true;
}

/** Parses resource text into a frozen array of names in first-occurrence order. */
function parse(text) {
  if (typeof text !== 'string') {
    throw new TypeError('text must be a string');
  }
  var names = [];
  var seen = new Set();
  var n = text.length;
  var i = 0;
  while (i < n) {
    var end = i;
    while (end < n && text.charCodeAt(end) !== 0x0A && text.charCodeAt(end) !== 0x0D) {
      end++;
    }
    var line = text.slice(i, end);
    if (end < n && text.charCodeAt(end) === 0x0D && end + 1 < n && text.charCodeAt(end + 1) === 0x0A) {
      i = end + 2;
    } else {
      i = end + 1;
    }
    var value = chars.toLowerCaseRoot(chars.javaTrim(line));
    if (value.length !== 0 && value.charCodeAt(0) !== 0x23) {
      if (!isHandlerName(value)) {
        throw new Error('Event-handler resource contains an invalid name');
      }
      if (!seen.has(value)) {
        seen.add(value);
        names.push(value);
      }
    }
  }
  if (names.length === 0) {
    throw new Error('Event-handler name resource is empty');
  }
  return Object.freeze(names);
}

var cached = null;

/** The 124 names from data/event-handler-names.txt, parsed once per process. */
function load() {
  if (cached !== null) {
    return cached;
  }
  var bytes;
  try {
    bytes = fs.readFileSync(RESOURCE);
  } catch (e) {
    if (e && e.code === 'ENOENT') {
      throw new Error('Event-handler name resource is missing');
    }
    throw new Error('Event-handler name resource cannot be read');
  }
  cached = parse(utf8.decodeLossy(bytes));
  return cached;
}

module.exports = {
  load: load,
  parse: parse,
  isHandlerName: isHandlerName
};
