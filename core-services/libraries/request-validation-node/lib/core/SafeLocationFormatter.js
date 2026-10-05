'use strict';

// Port of SafeLocationFormatter: locations never carry request text. A segment survives only if it is 1-64 units
// of [A-Za-z0-9_.-]; anything else becomes "*".

var MAX_SAFE_SEGMENT = 64;

/** [A-Za-z0-9_.-]{1,64}, by character scan. Non-strings are never safe. */
function isSafeSegment(value) {
  if (typeof value !== 'string' || value.length === 0 || value.length > MAX_SAFE_SEGMENT) {
    return false;
  }
  for (var index = 0; index < value.length; index++) {
    var c = value.charCodeAt(index);
    var safe = (c >= 0x61 && c <= 0x7A) || (c >= 0x41 && c <= 0x5A) || (c >= 0x30 && c <= 0x39)
        || c === 0x5F || c === 0x2E || c === 0x2D;
    if (!safe) {
      return false;
    }
  }
  return true;
}

/** The segment itself when safe, else "*". */
function scalar(value) {
  return isSafeSegment(value) ? value : '*';
}

/**
 * "/" for an empty list, else "/" + scalar(segment) for each segment. Array indexes are segments too and must be
 * passed as their decimal strings ("0", "1", ...), as the Java inspector does.
 */
function format(segments) {
  if (!Array.isArray(segments)) {
    throw new TypeError('segments must be an array');
  }
  if (segments.length === 0) {
    return '/';
  }
  var result = '';
  for (var i = 0; i < segments.length; i++) {
    result += '/' + scalar(segments[i]);
  }
  return result;
}

/**
 * Re-sanitizes an externally supplied location (the Violation constructor does this): null or empty gives "*"; a
 * value not starting with "/" is one bare segment; "/" stays; otherwise every "/"-separated part goes through
 * scalar(), keeping empty parts as "*".
 */
function sanitizeLocation(value) {
  if (typeof value !== 'string' || value.length === 0) {
    return '*';
  }
  if (value.charCodeAt(0) !== 0x2F) {
    return scalar(value);
  }
  if (value === '/') {
    return value;
  }
  var parts = value.slice(1).split('/');
  var safe = '';
  for (var i = 0; i < parts.length; i++) {
    safe += '/' + scalar(parts[i]);
  }
  return safe;
}

module.exports = {
  MAX_SAFE_SEGMENT: MAX_SAFE_SEGMENT,
  isSafeSegment: isSafeSegment,
  scalar: scalar,
  format: format,
  sanitizeLocation: sanitizeLocation
};
