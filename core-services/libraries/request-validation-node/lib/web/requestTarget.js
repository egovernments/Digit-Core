'use strict';

// The path a request is routed on. Express 4 and 5 match routes against parseurl(req).pathname: the request target
// up to the first '?' or, when the target contains '#', a space, TAB, LF, FF, CR, U+00A0 or U+FEFF, or does not
// start with '/', the pathname that Node's url.parse() returns. url.parse() drops the fragment, trims the target,
// turns '\' into '/' before the query, removes an authority and escapes some characters. Route policies are resolved
// on that same path, so a fragment, a trailing space or a backslash cannot select another policy than the route
// Express runs. Character scans only; no regular expression runs on the target.

var nodeVersion = String(process.versions.node).split('.');
var NODE_MAJOR = parseInt(nodeVersion[0], 10);

// url.parse() rules that changed between Node releases.
var TRIM_ALL_CONTROLS = NODE_MAJOR >= 16;       // trims every unit below U+0021 (before: SP, TAB, LF, FF, CR only)
var AT_DISABLES_SIMPLE_PATH = NODE_MAJOR >= 16; // an '@' before the query or fragment skips the simple-path case
var HOST_DROPS_TAB_LF_CR = NODE_MAJOR >= 18;    // TAB, LF and CR are removed from the authority
// A hostname ends at / \ # ? : (before Node 18: at any unit below U+0080 outside [A-Za-z0-9.+_-]).
var HOST_SPLITS_ON_DELIMITERS = NODE_MAJOR >= 18;
var HOSTLESS_BY_RAW_SCHEME = hostlessUsesRawScheme(); // early Node 8 releases compare the scheme case-sensitively

function hostlessUsesRawScheme() {
  if (NODE_MAJOR !== 8) {
    return false;
  }
  try {
    var parse = require('url').Url.prototype.parse;
    return Function.prototype.toString.call(parse).indexOf('hostlessProtocol[proto]') !== -1;
  } catch (e) {
    return false;
  }
}

// url.parse() escapes these units in the path, query and fragment.
var ESCAPED = {};
[[0x09, '%09'], [0x0A, '%0A'], [0x0D, '%0D'], [0x20, '%20'], [0x22, '%22'], [0x27, '%27'], [0x3C, '%3C'],
  [0x3E, '%3E'], [0x5C, '%5C'], [0x5E, '%5E'], [0x60, '%60'], [0x7B, '%7B'], [0x7C, '%7C'], [0x7D, '%7D']]
    .forEach(function (pair) {
      ESCAPED[pair[0]] = pair[1];
    });

function isTrimmed(c) {
  if (c === 0xA0 || c === 0xFEFF) {
    return true;
  }
  return TRIM_ALL_CONTROLS ? c < 0x21 : (c === 0x20 || c === 0x09 || c === 0x0A || c === 0x0C || c === 0x0D);
}

/** The units of the ECMAScript \s class. */
function isRegExpSpace(c) {
  return c === 0x20 || (c >= 0x09 && c <= 0x0D) || c === 0xA0 || c === 0x1680 || (c >= 0x2000 && c <= 0x200A)
      || c === 0x2028 || c === 0x2029 || c === 0x202F || c === 0x205F || c === 0x3000 || c === 0xFEFF;
}

function isAsciiAlnum(c) {
  return (c >= 0x30 && c <= 0x39) || (c >= 0x41 && c <= 0x5A) || (c >= 0x61 && c <= 0x7A);
}

function asciiLower(value) {
  var out = '';
  for (var i = 0; i < value.length; i++) {
    var c = value.charCodeAt(i);
    out += c >= 0x41 && c <= 0x5A ? String.fromCharCode(c + 0x20) : value.charAt(i);
  }
  return out;
}

/** The simple-path case of url.parse(): '/' or '//' not followed by '/', and no \s unit anywhere. */
function simplePath(rest) {
  if (rest.charCodeAt(0) !== 0x2F || (rest.charCodeAt(1) === 0x2F && rest.charCodeAt(2) === 0x2F)) {
    return null;
  }
  var question = -1;
  for (var i = 0; i < rest.length; i++) {
    var c = rest.charCodeAt(i);
    if (isRegExpSpace(c)) {
      return null;
    }
    if (c === 0x3F && question === -1) {
      question = i;
    }
  }
  return question === -1 ? rest : rest.slice(0, question);
}

/** The length of a leading URL scheme with its ':' ([A-Za-z0-9.+-]+:), or 0. */
function schemeLength(rest) {
  var i = 0;
  while (i < rest.length) {
    var c = rest.charCodeAt(i);
    if (!(isAsciiAlnum(c) || c === 0x2E || c === 0x2B || c === 0x2D)) {
      break;
    }
    i++;
  }
  return i > 0 && rest.charCodeAt(i) === 0x3A ? i + 1 : 0;
}

/** '//' + one or more units other than '@' and '/', '@', then one or more of them again. */
function looksLikeUserAtHost(rest) {
  if (rest.charCodeAt(0) !== 0x2F || rest.charCodeAt(1) !== 0x2F) {
    return false;
  }
  var i = 2;
  while (i < rest.length && rest.charCodeAt(i) !== 0x40 && rest.charCodeAt(i) !== 0x2F) {
    i++;
  }
  if (i === 2 || rest.charCodeAt(i) !== 0x40) {
    return false;
  }
  var j = i + 1;
  return j < rest.length && rest.charCodeAt(j) !== 0x40 && rest.charCodeAt(j) !== 0x2F;
}

function isNonHostUnit(c) {
  switch (c) {
    case 0x09: case 0x0A: case 0x0D: case 0x20: case 0x22: case 0x25: case 0x27: case 0x3B: case 0x3C:
    case 0x3E: case 0x5C: case 0x5E: case 0x60: case 0x7B: case 0x7C: case 0x7D:
      return true;
    default:
      return false;
  }
}

function isLegacyHostnameUnit(c) {
  return isAsciiAlnum(c) || c === 0x2E || c === 0x2D || c === 0x2B || c === 0x5F || c > 0x7F;
}

function isModernHostnameUnit(c) {
  return c !== 0x2F && c !== 0x5C && c !== 0x23 && c !== 0x3F && c !== 0x3A;
}

/** The pathname url.parse(url) returns (null when it has none). */
function urlParsePathname(url) {
  // Trim, and turn '\' into '/' before the first '?' or '#'.
  var hasHash = false;
  var hasAt = false;
  var start = -1;
  var end = -1;
  var rest = '';
  var lastPos = 0;
  var inWs = false;
  var split = false;
  var i;
  for (i = 0; i < url.length; i++) {
    var code = url.charCodeAt(i);
    var ws = isTrimmed(code);
    if (start === -1) {
      if (ws) {
        continue;
      }
      lastPos = start = i;
    } else if (inWs) {
      if (!ws) {
        end = -1;
        inWs = false;
      }
    } else if (ws) {
      end = i;
      inWs = true;
    }
    if (!split) {
      if (code === 0x40) {
        hasAt = true;
      } else if (code === 0x23) {
        hasHash = true;
        split = true;
      } else if (code === 0x3F) {
        split = true;
      } else if (code === 0x5C) {
        if (i - lastPos > 0) {
          rest += url.slice(lastPos, i);
        }
        rest += '/';
        lastPos = i + 1;
      }
    } else if (!hasHash && code === 0x23) {
      hasHash = true;
    }
  }
  if (start !== -1) {
    if (lastPos === start) {
      rest = end === -1 ? url.slice(start) : url.slice(start, end);
    } else if (end === -1 && lastPos < url.length) {
      rest += url.slice(lastPos);
    } else if (end !== -1 && lastPos < end) {
      rest += url.slice(lastPos, end);
    }
  }

  if (!hasHash && !(AT_DISABLES_SIMPLE_PATH && hasAt)) {
    var simple = simplePath(rest);
    if (simple !== null) {
      return simple;
    }
  }

  var proto = '';
  var lowerProto = '';
  var protoLength = schemeLength(rest);
  if (protoLength > 0) {
    proto = rest.slice(0, protoLength);
    lowerProto = asciiLower(proto);
    rest = rest.slice(protoLength);
  }
  var hostless = (HOSTLESS_BY_RAW_SCHEME ? proto : lowerProto) === 'javascript:';
  var slashes = false;
  if (proto !== '' || looksLikeUserAtHost(rest)) {
    slashes = rest.charCodeAt(0) === 0x2F && rest.charCodeAt(1) === 0x2F;
    if (slashes && !(proto !== '' && hostless)) {
      rest = rest.slice(2);
    }
  }
  var hostname = '';
  var slashedProto = lowerProto === 'http:' || lowerProto === 'https:' || lowerProto === 'ftp:'
      || lowerProto === 'gopher:' || lowerProto === 'file:';
  var slashedRawProto = proto === 'http:' || proto === 'https:' || proto === 'ftp:' || proto === 'gopher:'
      || proto === 'file:';
  if (!hostless && (slashes || (proto !== '' && !slashedRawProto))) {
    var atSign = -1;
    var nonHost = -1;
    for (i = 0; i < rest.length; i++) {
      var h = rest.charCodeAt(i);
      if (HOST_DROPS_TAB_LF_CR && (h === 0x09 || h === 0x0A || h === 0x0D)) {
        rest = rest.slice(0, i) + rest.slice(i + 1);
        i -= 1;
        continue;
      }
      if (isNonHostUnit(h)) {
        if (nonHost === -1) {
          nonHost = i;
        }
      } else if (h === 0x23 || h === 0x2F || h === 0x3F) {
        if (nonHost === -1) {
          nonHost = i;
        }
        break; // the authority ends here
      } else if (h === 0x40) {
        atSign = i;
        nonHost = -1;
      }
    }
    var hostStart = atSign === -1 ? 0 : atSign + 1;
    var host;
    if (nonHost === -1) {
      host = rest.slice(hostStart);
      rest = '';
    } else {
      host = rest.slice(hostStart, nonHost);
      rest = rest.slice(nonHost);
    }
    // A trailing ':' and digits are the port.
    var p = host.length;
    while (p > 0 && host.charCodeAt(p - 1) >= 0x30 && host.charCodeAt(p - 1) <= 0x39) {
      p--;
    }
    if (p > 0 && host.charCodeAt(p - 1) === 0x3A) {
      host = host.slice(0, p - 1);
    }
    hostname = host;
    var ipv6 = hostname.charCodeAt(0) === 0x5B && hostname.charCodeAt(hostname.length - 1) === 0x5D;
    if (!ipv6) {
      for (var k = 0; k < hostname.length; k++) {
        var u = hostname.charCodeAt(k);
        if (!(HOST_SPLITS_ON_DELIMITERS ? isModernHostnameUnit(u) : isLegacyHostnameUnit(u))) {
          rest = '/' + hostname.slice(k) + rest;
          hostname = hostname.slice(0, k);
          break;
        }
      }
    }
    if (hostname.length > 255) {
      hostname = '';
    }
    if (ipv6 && rest.charCodeAt(0) !== 0x2F) {
      rest = '/' + rest;
    }
  }

  if (lowerProto !== 'javascript:') {
    var escaped = '';
    var from = 0;
    for (i = 0; i < rest.length; i++) {
      var e = ESCAPED[rest.charCodeAt(i)];
      if (e !== undefined) {
        escaped += rest.slice(from, i) + e;
        from = i + 1;
      }
    }
    if (from !== 0) {
      rest = escaped + rest.slice(from);
    }
  }

  var firstIdx = -1;
  for (i = 0; i < rest.length; i++) {
    var d = rest.charCodeAt(i);
    if (d === 0x23 || d === 0x3F) {
      firstIdx = i;
      break;
    }
  }
  var pathname = null;
  if (firstIdx === -1) {
    if (rest.length > 0) {
      pathname = rest;
    }
  } else if (firstIdx > 0) {
    pathname = rest.slice(0, firstIdx);
  }
  if (slashedProto && hostname !== '' && !pathname) {
    pathname = '/';
  }
  return pathname;
}

/** parseurl's fastparse(target).pathname: the target before '?', unless url.parse() is needed. */
function routedPathOf(target) {
  if (typeof target !== 'string') {
    return null;
  }
  if (target.charCodeAt(0) !== 0x2F) {
    return urlParsePathname(target);
  }
  var question = -1;
  for (var i = 1; i < target.length; i++) {
    var c = target.charCodeAt(i);
    if (c === 0x3F) {
      if (question === -1) {
        question = i;
      }
    } else if (c === 0x09 || c === 0x0A || c === 0x0C || c === 0x0D || c === 0x20 || c === 0x23 || c === 0xA0
        || c === 0xFEFF) {
      return urlParsePathname(target);
    }
  }
  return question === -1 ? target : target.slice(0, question);
}

/** The raw request target: req.originalUrl (Express) or req.url. */
function rawTarget(req) {
  var target = req.originalUrl || req.url;
  return typeof target === 'string' ? target : '';
}

// A parse result parseurl kept on the request for exactly this target, or null.
function cachedParse(parsed, target) {
  if (parsed !== null && typeof parsed === 'object' && parsed._raw === target
      && (typeof parsed.pathname === 'string' || parsed.pathname === null)) {
    return parsed;
  }
  return null;
}

/**
 * The pathname Express routes `req` on ('' when the target has no pathname) and the query string its query parser
 * reads (null when there is none). When the router has already parsed this very target (parseurl keeps its result
 * on the request), that result is used; otherwise the pathname is computed and the query is the raw text after the
 * first '?'.
 */
function routedTarget(req, target) {
  var parsed = (req.url === target ? cachedParse(req._parsedUrl, target) : null)
      || (req.originalUrl === target ? cachedParse(req._parsedOriginalUrl, target) : null);
  var path = parsed !== null ? parsed.pathname : routedPathOf(target);
  var query;
  if (parsed !== null) {
    query = typeof parsed.query === 'string' ? parsed.query : null;
  } else {
    var q = target.indexOf('?');
    query = q === -1 ? null : target.slice(q + 1);
  }
  return { pathname: typeof path === 'string' ? path : '', query: query };
}

function routedPathname(req, target) {
  return routedTarget(req, target).pathname;
}

module.exports = {
  rawTarget: rawTarget,
  routedTarget: routedTarget,
  routedPathname: routedPathname,
  routedPathOf: routedPathOf,
  urlParsePathname: urlParsePathname
};
