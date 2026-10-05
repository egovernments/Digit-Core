'use strict';

// Port of SkipPathMatcher: JSON-Pointer-style subtree patterns. A pattern matches its own path and every
// descendant; "*" matches exactly one segment; "/" is the property with the empty name, not the whole document.

function decodeSegment(segment) {
  var decoded = '';
  for (var index = 0; index < segment.length; index++) {
    var c = segment.charAt(index);
    if (c !== '~') {
      decoded += c;
      continue;
    }
    if (index + 1 >= segment.length) {
      throw new RangeError('Invalid JSON Pointer escape');
    }
    var escaped = segment.charAt(++index);
    if (escaped === '0') {
      decoded += '~';
    } else if (escaped === '1') {
      decoded += '/';
    } else {
      throw new RangeError('Invalid JSON Pointer escape');
    }
  }
  return decoded;
}

function parse(pointer) {
  if (typeof pointer !== 'string') {
    throw new TypeError('skip path must be a string');
  }
  if (pointer.length === 0) {
    // "" is the whole-document pointer; allowing it would exclude the whole body without a reason.
    throw new RangeError('Empty skip path excludes the whole body; use structured = DISABLED with a reason');
  }
  if (pointer.charCodeAt(0) !== 0x2F) {
    throw new RangeError('Skip path must be a JSON Pointer');
  }
  var encoded = pointer.slice(1).split('/');
  var decoded = [];
  for (var i = 0; i < encoded.length; i++) {
    var value = decodeSegment(encoded[i]);
    if (value.indexOf('*') >= 0 && value !== '*') {
      throw new RangeError('Wildcard must occupy an entire path segment');
    }
    decoded.push(value);
  }
  return Object.freeze(decoded);
}

class SkipPathMatcher {
  /** @param {string[]} pointers skip paths; each must be a non-empty JSON Pointer. */
  constructor(pointers) {
    if (!Array.isArray(pointers)) {
      throw new TypeError('pointers must be an array');
    }
    var compiled = [];
    for (var i = 0; i < pointers.length; i++) {
      compiled.push(parse(pointers[i]));
    }
    this.pointers = Object.freeze(pointers.slice());
    this.patterns = Object.freeze(compiled);
    Object.freeze(this);
  }

  /** @param {string[]} path the raw (unescaped) segments of a location, array indexes as decimal strings. */
  matches(path) {
    if (!Array.isArray(path)) {
      throw new TypeError('path must be an array');
    }
    var patterns = this.patterns;
    for (var p = 0; p < patterns.length; p++) {
      var pattern = patterns[p];
      if (pattern.length > path.length) {
        continue;
      }
      var match = true;
      for (var index = 0; index < pattern.length; index++) {
        var expected = pattern[index];
        if (expected !== '*' && expected !== path[index]) {
          match = false;
          break;
        }
      }
      if (match) {
        return true;
      }
    }
    return false;
  }

  /** True when there are no patterns (nothing is ever skipped). */
  get isEmpty() {
    return this.patterns.length === 0;
  }
}

module.exports = SkipPathMatcher;
