'use strict';

// Conversion of configuration text with the rules Spring Boot 2.2 applies to the Java library's settings:
// placeholder resolution, blank values, booleans, decimal or hexadecimal integers, Java doubles, lenient enum
// names and comma-separated lists. Environment variables and string-valued options go through these functions.

var chars = require('../core/jdk/javaChars');

function fail(message) {
  return new RangeError(message);
}

// ---------------------------------------------------------------------------------------------------------------
// Environment lookup

/**
 * A lookup function over an environment object. Like Spring's SystemEnvironmentPropertySource, a name that is not
 * present as given is also tried with '.' and/or '-' replaced by '_', and then in upper case.
 */
function environmentLookup(env) {
  if (!env) {
    return function () {
      return null;
    };
  }
  function has(name) {
    return Object.prototype.hasOwnProperty.call(env, name) && env[name] !== undefined && env[name] !== null;
  }
  function check(name) {
    if (has(name)) {
      return name;
    }
    var noDot = name.split('.').join('_');
    if (noDot !== name && has(noDot)) {
      return noDot;
    }
    var noHyphen = name.split('-').join('_');
    if (noHyphen !== name && has(noHyphen)) {
      return noHyphen;
    }
    var neither = noDot.split('-').join('_');
    if (neither !== noDot && has(neither)) {
      return neither;
    }
    return null;
  }
  return function lookup(name) {
    var found = check(name);
    if (found === null) {
      var upper = asciiUpper(name);
      if (upper !== name) {
        found = check(upper);
      }
    }
    return found === null ? null : String(env[found]);
  };
}

function asciiUpper(value) {
  var out = '';
  for (var i = 0; i < value.length; i++) {
    var c = value.charCodeAt(i);
    out += c >= 0x61 && c <= 0x7A ? String.fromCharCode(c - 0x20) : value.charAt(i);
  }
  return out;
}

function asciiLower(value) {
  var out = '';
  for (var i = 0; i < value.length; i++) {
    var c = value.charCodeAt(i);
    out += c >= 0x41 && c <= 0x5A ? String.fromCharCode(c + 0x20) : value.charAt(i);
  }
  return out;
}

// ---------------------------------------------------------------------------------------------------------------
// Placeholders: ${NAME} and ${NAME:default}, resolved recursively (Spring PropertyPlaceholderHelper)

function findPlaceholderEnd(text, start) {
  var index = start + 2;
  var nested = 0;
  while (index < text.length) {
    if (text.charAt(index) === '}') {
      if (nested > 0) {
        nested--;
        index++;
      } else {
        return index;
      }
    } else if (text.charAt(index) === '{') {
      nested++;
      index++;
    } else {
      index++;
    }
  }
  return -1;
}

/**
 * Resolves ${...} placeholders against `lookup`. With `strict` false an unresolvable placeholder is left as it is
 * (Spring's binder behaviour); with `strict` true it throws. A circular reference always throws.
 */
function resolvePlaceholders(value, lookup, strict, visited) {
  var startIndex = value.indexOf('${');
  if (startIndex === -1) {
    return value;
  }
  var seen = visited || [];
  var result = value;
  while (startIndex !== -1) {
    var endIndex = findPlaceholderEnd(result, startIndex);
    if (endIndex === -1) {
      break;
    }
    var original = result.slice(startIndex + 2, endIndex);
    if (seen.indexOf(original) !== -1) {
      throw fail('Circular placeholder reference \'' + original + '\'');
    }
    seen.push(original);
    var placeholder = resolvePlaceholders(original, lookup, strict, seen);
    var resolved = lookup(placeholder);
    if (resolved === null) {
      var separator = placeholder.indexOf(':');
      if (separator !== -1) {
        resolved = lookup(placeholder.slice(0, separator));
        if (resolved === null) {
          resolved = placeholder.slice(separator + 1);
        }
      }
    }
    if (resolved !== null) {
      resolved = resolvePlaceholders(resolved, lookup, strict, seen);
      result = result.slice(0, startIndex) + resolved + result.slice(endIndex + 1);
      startIndex = result.indexOf('${', startIndex + resolved.length);
    } else if (!strict) {
      startIndex = result.indexOf('${', endIndex + 1);
    } else {
      throw fail('Could not resolve placeholder \'' + placeholder + '\'');
    }
    seen.splice(seen.indexOf(original), 1);
  }
  return result;
}

// ---------------------------------------------------------------------------------------------------------------
// Blank values

/** String.trim().isEmpty(): empty or only units <= U+0020. */
function isBlank(value) {
  return chars.javaTrim(value).length === 0;
}

/**
 * The value of a setting given as text, after placeholder resolution, or undefined when the text means "not set":
 * a blank scalar, or a list that is blank but not empty (an empty list value is an empty list). Mirrors which
 * variables the Java library declares for Spring to bind (EnvironmentVariableFallback).
 */
function settingText(raw, lookup, isList) {
  var resolved = resolvePlaceholders(raw, lookup, false);
  if (isBlank(resolved) && !(resolved.length === 0 && isList)) {
    return undefined;
  }
  return resolved;
}

// ---------------------------------------------------------------------------------------------------------------
// Converters (Spring's StringToBooleanConverter, NumberUtils.parseNumber, Double.valueOf,
// LenientStringToEnumConverterFactory, StringToCollectionConverter)

/** StringUtils.trimAllWhitespace: removes every Character.isWhitespace unit. */
function trimAllWhitespace(value) {
  var out = '';
  for (var i = 0; i < value.length; i++) {
    if (!chars.isWhitespace(value.charCodeAt(i))) {
      out += value.charAt(i);
    }
  }
  return out;
}

var TRUE_VALUES = ['true', 'on', 'yes', '1'];
var FALSE_VALUES = ['false', 'off', 'no', '0'];

/** true|on|yes|1 and false|off|no|0, trimmed, case-insensitive. Returns null for an empty value. */
function toBoolean(text) {
  var value = chars.javaTrim(text);
  if (value.length === 0) {
    return null;
  }
  var lower = asciiLower(value);
  if (TRUE_VALUES.indexOf(lower) !== -1) {
    return true;
  }
  if (FALSE_VALUES.indexOf(lower) !== -1) {
    return false;
  }
  throw fail('invalid boolean value');
}

function digitValue(c, radix) {
  var v = -1;
  if (c >= 0x30 && c <= 0x39) {
    v = c - 0x30;
  } else if (c >= 0x61 && c <= 0x7A) {
    v = c - 0x61 + 10;
  } else if (c >= 0x41 && c <= 0x5A) {
    v = c - 0x41 + 10;
  }
  return v >= 0 && v < radix ? v : -1;
}

// Integer.parseInt / Long.parseLong: an optional sign, then at least one digit as Character.digit(char, radix)
// reads it (so any Unicode decimal digit, and fullwidth Latin letters for hexadecimal), range-checked.
function parseRadix(text, radix, bits) {
  if (text.length === 0) {
    throw fail('invalid number');
  }
  var i = 0;
  var negative = false;
  var first = text.charAt(0);
  if (first === '-' || first === '+') {
    negative = first === '-';
    i = 1;
    if (text.length === 1) {
      throw fail('invalid number');
    }
  }
  var max = bits === 64 ? 9223372036854775807 : 2147483647;
  var magnitude = 0;
  for (; i < text.length; i++) {
    var d = chars.digit(text.charCodeAt(i), radix);
    if (d < 0) {
      throw fail('invalid number');
    }
    magnitude = magnitude * radix + d;
    if (magnitude > max + 1) {
      throw fail('number out of range');
    }
  }
  if (negative ? magnitude > max + 1 : magnitude > max) {
    throw fail('number out of range');
  }
  return negative ? -magnitude : magnitude;
}

/**
 * Spring's NumberUtils.parseNumber for Integer (bits 32) or Long (bits 64): all whitespace removed, then a value
 * with a 0x, 0X or # prefix (after an optional '-') is hexadecimal, any other value decimal.
 * Returns null for an empty value.
 */
function toInteger(text, bits) {
  if (text.length === 0) {
    return null;
  }
  var trimmed = trimAllWhitespace(text);
  var index = trimmed.charAt(0) === '-' ? 1 : 0;
  var hex = trimmed.substr(index, 2) === '0x' || trimmed.substr(index, 2) === '0X' || trimmed.charAt(index) === '#';
  if (!hex) {
    return parseRadix(trimmed, 10, bits || 32);
  }
  // Integer.decode / Long.decode
  var negative = index === 1;
  var digitsStart = index + (trimmed.charAt(index) === '#' ? 1 : 2);
  var digits = trimmed.slice(digitsStart);
  if (digits.charAt(0) === '-' || digits.charAt(0) === '+') {
    throw fail('invalid number');
  }
  return parseRadix((negative ? '-' : '') + digits, 16, bits || 32);
}

function isAsciiDigit(c) {
  return c >= 0x30 && c <= 0x39;
}

function isHexDigit(c) {
  return digitValue(c, 16) >= 0;
}

function hexFloatValue(intPart, fracPart, exponent) {
  var mantissa = 0;
  var scale = 0;
  var digits = intPart + fracPart;
  var significant = 0;
  for (var i = 0; i < digits.length; i++) {
    var d = digitValue(digits.charCodeAt(i), 16);
    if (significant < 15) {
      if (mantissa !== 0 || d !== 0) {
        significant++;
      }
      mantissa = mantissa * 16 + d;
      if (i >= intPart.length) {
        scale -= 4;
      }
    } else if (i < intPart.length) {
      scale += 4;
    }
  }
  return mantissa * Math.pow(2, exponent + scale);
}

/**
 * Double.valueOf after Spring's whitespace removal: optional sign, NaN, Infinity, decimal digits with an optional
 * fraction and exponent, or a hexadecimal significand with a binary exponent, each with an optional f/F/d/D suffix.
 * Returns null for an empty value.
 */
function toDouble(text) {
  if (text.length === 0) {
    return null;
  }
  return parseJavaDouble(trimAllWhitespace(text));
}

/** Double.parseDouble(value) (Java FloatingDecimal grammar). Throws RangeError when the text is not a double. */
function parseJavaDouble(value) {
  var s = chars.javaTrim(value);
  var n = s.length;
  var i = 0;
  var sign = 1;
  if (i < n && (s.charAt(i) === '+' || s.charAt(i) === '-')) {
    sign = s.charAt(i) === '-' ? -1 : 1;
    i++;
  }
  if (s.slice(i) === 'NaN') {
    return NaN;
  }
  if (s.slice(i) === 'Infinity') {
    return sign * Infinity;
  }
  if (i + 1 < n && s.charAt(i) === '0' && (s.charAt(i + 1) === 'x' || s.charAt(i + 1) === 'X')) {
    i += 2;
    var hexStart = i;
    while (i < n && isHexDigit(s.charCodeAt(i))) {
      i++;
    }
    var hexInt = s.slice(hexStart, i);
    var hexFrac = '';
    if (i < n && s.charAt(i) === '.') {
      i++;
      var fracStart = i;
      while (i < n && isHexDigit(s.charCodeAt(i))) {
        i++;
      }
      hexFrac = s.slice(fracStart, i);
    }
    if (hexInt.length + hexFrac.length === 0 || i >= n || (s.charAt(i) !== 'p' && s.charAt(i) !== 'P')) {
      throw fail('invalid number');
    }
    i++;
    var expSign = 1;
    if (i < n && (s.charAt(i) === '+' || s.charAt(i) === '-')) {
      expSign = s.charAt(i) === '-' ? -1 : 1;
      i++;
    }
    var expStart = i;
    while (i < n && isAsciiDigit(s.charCodeAt(i))) {
      i++;
    }
    if (i === expStart) {
      throw fail('invalid number');
    }
    var binaryExponent = expSign * Math.min(Number(s.slice(expStart, i)), 100000);
    if (i < n && 'fFdD'.indexOf(s.charAt(i)) !== -1) {
      i++;
    }
    if (i !== n) {
      throw fail('invalid number');
    }
    return sign * hexFloatValue(hexInt, hexFrac, binaryExponent);
  }
  var start = i;
  var digits = 0;
  while (i < n && isAsciiDigit(s.charCodeAt(i))) {
    i++;
    digits++;
  }
  if (i < n && s.charAt(i) === '.') {
    i++;
    while (i < n && isAsciiDigit(s.charCodeAt(i))) {
      i++;
      digits++;
    }
  }
  if (digits === 0) {
    throw fail('invalid number');
  }
  var mantissaEnd = i;
  var exponentText = '';
  if (i < n && (s.charAt(i) === 'e' || s.charAt(i) === 'E')) {
    i++;
    var eStart = i;
    if (i < n && (s.charAt(i) === '+' || s.charAt(i) === '-')) {
      i++;
    }
    var eDigits = i;
    while (i < n && isAsciiDigit(s.charCodeAt(i))) {
      i++;
    }
    if (i === eDigits) {
      throw fail('invalid number');
    }
    exponentText = 'e' + s.slice(eStart, i);
  }
  if (i < n && 'fFdD'.indexOf(s.charAt(i)) !== -1) {
    i++;
  }
  if (i !== n) {
    throw fail('invalid number');
  }
  var mantissa = s.slice(start, mantissaEnd);
  if (mantissa.charAt(mantissa.length - 1) === '.') {
    mantissa += '0';
  }
  if (mantissa.charAt(0) === '.') {
    mantissa = '0' + mantissa;
  }
  return sign * Number(mantissa + exponentText);
}

/**
 * Spring Boot's lenient enum conversion: an exact name, else the name compared ignoring case and every character
 * that is not a letter or digit ("enforce", "En-Force" and "en_force" are ENFORCE). Returns null for an empty value.
 * Values with non-ASCII characters other than Java whitespace match nothing.
 */
function toEnum(text, names) {
  if (text.length === 0) {
    return null;
  }
  var value = chars.javaTrim(text);
  if (names.indexOf(value) !== -1) {
    return value;
  }
  var canonical = canonicalEnumName(value);
  for (var i = 0; i < names.length; i++) {
    if (canonicalEnumName(names[i]) === canonical) {
      return names[i];
    }
  }
  throw fail('invalid value');
}

function canonicalEnumName(value) {
  var out = '';
  for (var i = 0; i < value.length; i++) {
    var c = value.charCodeAt(i);
    if ((c >= 0x30 && c <= 0x39) || (c >= 0x61 && c <= 0x7A)) {
      out += value.charAt(i);
    } else if (c >= 0x41 && c <= 0x5A) {
      out += String.fromCharCode(c + 0x20);
    } else if (c >= 0x80 && !chars.isWhitespace(c) && !chars.isISOControl(c)) {
      out += value.charAt(i);
    }
  }
  return out;
}

/** Spring's comma-delimited list: split on ',', each element trimmed; an empty value is an empty list. */
function splitList(text) {
  if (text.length === 0) {
    return [];
  }
  return text.split(',').map(function (element) {
    return chars.javaTrim(element);
  });
}

module.exports = {
  environmentLookup: environmentLookup,
  resolvePlaceholders: resolvePlaceholders,
  settingText: settingText,
  isBlank: isBlank,
  trimAllWhitespace: trimAllWhitespace,
  toBoolean: toBoolean,
  toInteger: toInteger,
  toDouble: toDouble,
  parseJavaDouble: parseJavaDouble,
  toEnum: toEnum,
  splitList: splitList,
  asciiLower: asciiLower,
  asciiUpper: asciiUpper
};
