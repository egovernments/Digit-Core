'use strict';

// Content-Type parsing with the rules of Spring 5.2 MediaType.parseMediaType (MimeTypeUtils.parseMimeType), the
// parser that decides in Java whether a body is JSON and what its charset is. One difference is deliberate: the
// charset parameter is not resolved as a Java charset name here; the adapter treats every charset that is not a
// UTF-8 name as a finding. Character scans only; no regular expression runs on header text.

var chars = require('../core/jdk/javaChars');
var conv = require('../config/env');

// RFC 2616 token characters: US-ASCII 33-126 except the separators ()<>@,;:\"/[]?={} (space and tab are excluded
// with the controls).
var TOKEN = new Uint8Array(128);
(function () {
  for (var c = 33; c < 127; c++) {
    TOKEN[c] = 1;
  }
  var separators = '()<>@,;:\\"/[]?={}';
  for (var i = 0; i < separators.length; i++) {
    TOKEN[separators.charCodeAt(i)] = 0;
  }
})();

/** Spring's checkToken plus hasLength: non-empty and only token characters. */
function isToken(value) {
  if (value.length === 0) {
    return false;
  }
  for (var i = 0; i < value.length; i++) {
    var c = value.charCodeAt(i);
    if (c >= 128 || TOKEN[c] === 0) {
      return false;
    }
  }
  return true;
}

/** Spring's isQuotedString: at least two characters, enclosed in double or in single quotes. */
function isQuoted(value) {
  if (value.length < 2) {
    return false;
  }
  var first = value.charAt(0);
  var last = value.charAt(value.length - 1);
  return (first === '"' && last === '"') || (first === '\'' && last === '\'');
}

function unquote(value) {
  return isQuoted(value) ? value.slice(1, -1) : value;
}

/**
 * Parses a Content-Type header value. Returns null when the value is absent, empty or not a valid media type for
 * Spring; otherwise { type, subtype } (lower case), `parameters` (array of [name, value] as received, in the order
 * Spring keeps them) and `charset` (the raw value of the charset parameter, any case, last one wins; undefined when
 * absent).
 */
function parse(value) {
  if (typeof value !== 'string' || value.length === 0) {
    return null;
  }
  var index = value.indexOf(';');
  var fullType = chars.javaTrim(index >= 0 ? value.slice(0, index) : value);
  if (fullType.length === 0) {
    return null;
  }
  if (fullType === '*') {
    fullType = '*/*';
  }
  var subIndex = fullType.indexOf('/');
  if (subIndex === -1 || subIndex === fullType.length - 1) {
    return null;
  }
  var type = fullType.slice(0, subIndex);
  var subtype = fullType.slice(subIndex + 1);
  if (type === '*' && subtype !== '*') {
    return null;
  }
  // Parameters: split at ';' outside double quotes; a parameter without '=' is ignored; for a repeated name the
  // last value wins at the position of the first (LinkedHashMap).
  var names = [];
  var values = [];
  do {
    var nextIndex = index + 1;
    var quoted = false;
    while (nextIndex < value.length) {
      var ch = value.charCodeAt(nextIndex);
      if (ch === 0x3B) {
        if (!quoted) {
          break;
        }
      } else if (ch === 0x22) {
        quoted = !quoted;
      }
      nextIndex++;
    }
    var parameter = chars.javaTrim(value.slice(index + 1, nextIndex));
    if (parameter.length > 0) {
      var eq = parameter.indexOf('=');
      if (eq >= 0) {
        var attribute = chars.javaTrim(parameter.slice(0, eq));
        var attributeValue = chars.javaTrim(parameter.slice(eq + 1));
        var existing = names.indexOf(attribute);
        if (existing >= 0) {
          values[existing] = attributeValue;
        } else {
          names.push(attribute);
          values.push(attributeValue);
        }
      }
    }
    index = nextIndex;
  } while (index < value.length);

  if (!isToken(type) || !isToken(subtype)) {
    return null;
  }
  var charset;
  var parameters = [];
  for (var i = 0; i < names.length; i++) {
    var name = names[i];
    var v = values[i];
    if (!isToken(name) || v.length === 0) {
      return null;
    }
    if (name !== 'charset' && !isQuoted(v) && !isToken(v)) {
      return null;
    }
    if (name === 'q') {
      // MediaType: the quality factor must be a Java double between 0 and 1
      var q;
      try {
        q = conv.parseJavaDouble(unquote(v));
      } catch (e) {
        return null;
      }
      if (!(q >= 0 && q <= 1)) {
        return null;
      }
    }
    var lowerName = conv.asciiLower(name);
    if (lowerName === 'charset') {
      charset = v;
    }
    // LinkedCaseInsensitiveMap: a later name that differs only in case replaces the earlier one
    for (var p = 0; p < parameters.length; p++) {
      if (conv.asciiLower(parameters[p][0]) === lowerName) {
        parameters.splice(p, 1);
        break;
      }
    }
    parameters.push([name, v]);
  }
  return {
    type: conv.asciiLower(type),
    subtype: conv.asciiLower(subtype),
    parameters: parameters,
    charset: charset
  };
}

/** StructuredBodyAdvice.isJson: application/json or application/*+json, any case. */
function isJson(mediaType) {
  if (!mediaType || mediaType.type !== 'application') {
    return false;
  }
  var s = mediaType.subtype;
  return s === 'json' || (s.length >= 5 && s.slice(-5) === '+json');
}

/** application/x-www-form-urlencoded, any case. */
function isForm(mediaType) {
  return !!mediaType && mediaType.type === 'application' && mediaType.subtype === 'x-www-form-urlencoded';
}

var UTF8_NAMES = ['utf-8', 'utf8', 'unicode-1-1-utf-8'];

/** True when the (raw, possibly quoted) charset parameter names UTF-8 for Java: UTF-8, UTF8 or unicode-1-1-utf-8. */
function isUtf8Charset(raw) {
  return UTF8_NAMES.indexOf(conv.asciiLower(unquote(raw))) !== -1;
}

/** True when an encoding name passed by a body parser to a verify hook is a UTF-8 name. */
function isUtf8Encoding(encoding) {
  return typeof encoding === 'string' && UTF8_NAMES.indexOf(conv.asciiLower(encoding)) !== -1;
}

/** type-is hasBody: a Transfer-Encoding header, or a numeric Content-Length. */
function hasBody(req) {
  var headers = req.headers || {};
  return headers['transfer-encoding'] !== undefined || !isNaN(headers['content-length']);
}

module.exports = {
  parse: parse,
  isJson: isJson,
  isForm: isForm,
  isUtf8Charset: isUtf8Charset,
  isUtf8Encoding: isUtf8Encoding,
  hasBody: hasBody,
  unquote: unquote,
  isToken: isToken
};
