'use strict';

// Port of org.egov.requestvalidation.core.FlaggedValue: a string value the content check flagged, with read access
// to the other string values of the same body. Lookups return null where Java returns Optional.empty().

var SafeLocationFormatter = require('./SafeLocationFormatter');

function escapeSegment(segment) {
  var out = '';
  for (var i = 0; i < segment.length; i++) {
    var c = segment.charAt(i);
    out += c === '~' ? '~0' : c === '/' ? '~1' : c;
  }
  return out;
}

/** The JSON Pointer of raw segments; '' for the root. */
function format(segments) {
  var pointer = '';
  for (var i = 0; i < segments.length; i++) {
    pointer += '/' + escapeSegment(segments[i]);
  }
  return pointer;
}

function parse(pointer) {
  if (typeof pointer !== 'string') {
    throw new TypeError('pointer');
  }
  if (pointer.length === 0) {
    return [];
  }
  if (pointer.charCodeAt(0) !== 0x2F) {
    throw new RangeError('Not a JSON Pointer');
  }
  var encoded = pointer.slice(1).split('/');
  var decoded = [];
  for (var i = 0; i < encoded.length; i++) {
    var segment = encoded[i];
    var value = '';
    for (var j = 0; j < segment.length; j++) {
      var c = segment.charAt(j);
      if (c !== '~') {
        value += c;
      } else if (segment.charAt(j + 1) === '0') {
        value += '~';
        j++;
      } else if (segment.charAt(j + 1) === '1') {
        value += '/';
        j++;
      } else {
        throw new RangeError('Invalid JSON Pointer escape');
      }
    }
    decoded.push(value);
  }
  return decoded;
}

class FlaggedValue {
  /**
   * @param {string[]} path raw segments of the value's location
   * @param {string} value
   * @param {string} rule R1..R4
   * @param {Map<string, string>} strings every string value of the body by JSON Pointer
   */
  constructor(path, value, rule, strings) {
    Object.defineProperty(this, 'path', { value: Object.freeze(path.slice()), enumerable: true });
    Object.defineProperty(this, 'value', { value: value, enumerable: true });
    Object.defineProperty(this, 'rule', { value: rule, enumerable: true });
    Object.defineProperty(this, 'strings', { value: strings, enumerable: false });
    Object.freeze(this);
  }

  /** The JSON Pointer of the value, e.g. /messages/3/message; '' for a bare string body. */
  get pointer() {
    return format(this.path);
  }

  /** True when the path has exactly the pattern's segments; a '*' segment matches any one segment. */
  matches(pattern) {
    var segments = parse(pattern);
    if (segments.length !== this.path.length) {
      return false;
    }
    for (var i = 0; i < segments.length; i++) {
      if (segments[i] !== '*' && segments[i] !== this.path[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * The string value at a JSON Pointer of the same body; null when absent or not a string. With duplicate keys
   * allowed (rejectDuplicateKeys false) the last occurrence wins, as in Jackson data binding.
   */
  string(pointer) {
    var found = this.strings.get(format(parse(pointer)));
    return found === undefined ? null : found;
  }

  /** The string value of another field of the object that holds this value; null for array elements. */
  sibling(name) {
    if (typeof name !== 'string') {
      throw new TypeError('name');
    }
    if (this.path.length === 0) {
      return null;
    }
    var target = this.path.slice(0, this.path.length - 1);
    target.push(name);
    var found = this.strings.get(format(target));
    return found === undefined ? null : found;
  }

  toString() {
    // Never the value: it is untrusted input.
    return 'FlaggedValue[rule=' + this.rule + ', location=' + SafeLocationFormatter.format(this.path)
        + ', length=' + this.value.length + ']';
  }
}

FlaggedValue.format = format;

module.exports = FlaggedValue;
