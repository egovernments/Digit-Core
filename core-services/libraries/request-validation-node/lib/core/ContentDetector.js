'use strict';

// Port of ContentDetector: rules R1-R4 over the value and its decoded variants. Every predicate uses Java 8
// character semantics (jdk/javaChars), the jsoup entity decoder, the Java lossy UTF-8 decoder and Java-8-masked
// NFKC, so verdicts match the Java library for the same value and policy.

var ContentPolicy = require('./ContentPolicy');
var EventHandlerNameList = require('./EventHandlerNameList');
var decoder = require('./EntityAndPercentDecoder');
var chars = require('./jdk/javaChars');
var nfkc = require('./jdk/nfkc');

var MAX_VARIANTS = 40;
var MAX_NORMALIZATION_EXPANSION = 18;
var INT_MAX = 2147483647;
var DATA_PREFIX = 'data:';

function growthLimit(originalLength) {
  var limit = Math.max(originalLength + 64, originalLength * MAX_NORMALIZATION_EXPANSION);
  return limit >= INT_MAX ? INT_MAX : limit;
}

/** Fast path: no '<', '&', '%', ':', '=' and no ISO control unit means no rule can match. */
function hasNoSentinel(value) {
  for (var index = 0; index < value.length; index++) {
    var c = value.charCodeAt(index);
    if (c === 0x3C || c === 0x26 || c === 0x25 || c === 0x3A || c === 0x3D || c <= 0x1F
        || (c >= 0x7F && c <= 0x9F)) {
      return false;
    }
  }
  return true;
}

function isAsciiLetter(c) {
  return (c >= 0x61 && c <= 0x7A) || (c >= 0x41 && c <= 0x5A);
}

function isAsciiWord(c) {
  return isAsciiLetter(c) || (c >= 0x30 && c <= 0x39) || c === 0x5F;
}

function isLeadingUrlSpaceOrControl(c) {
  return c <= 0x20 || c === 0x7F || chars.isWhitespace(c);
}

/** R1: some '<' is followed by an ASCII letter, '/', '!' or '?'. */
function hasMarkupStart(value) {
  var n = value.length;
  for (var index = value.indexOf('<'); index >= 0 && index + 1 < n; index = value.indexOf('<', index + 1)) {
    var next = value.charCodeAt(index + 1);
    if (isAsciiLetter(next) || next === 0x2F || next === 0x21 || next === 0x3F) {
      return true;
    }
  }
  return false;
}

/**
 * The leading units of Java's canonicalizeUrlPrefix(value): skip leading URL space or control units, drop TAB, LF
 * and CR anywhere, map each unit through Character.toLowerCase(char), then map each code point of that result
 * through String.toLowerCase(Locale.ROOT). Generation stops once the result holds at least `need` units (whole code
 * points only), or, with `toDataEnd`, once it holds a ';' or ',' at index 5 or later (the end of a data: URL's media
 * type). One pass; runs of units that map to themselves are copied as slices.
 */
function canonicalUrlPrefix(value, need, toDataEnd) {
  var n = value.length;
  var index = 0;
  while (index < n && isLeadingUrlSpaceOrControl(value.charCodeAt(index))) {
    index++;
  }
  var pieces = [];
  var length = 0;      // units in pieces
  var runStart = -1;   // value[runStart, runEnd) maps to itself and is not in pieces yet
  var runEnd = -1;
  function flush() {
    if (runStart !== -1) {
      pieces.push(value.slice(runStart, runEnd));
      length += runEnd - runStart;
      runStart = -1;
    }
  }
  function same(i) {
    if (runStart !== -1 && runEnd === i) {
      runEnd = i + 1;
    } else {
      flush();
      runStart = i;
      runEnd = i + 1;
    }
  }
  function text(t) {
    flush();
    pieces.push(t);
    length += t.length;
  }
  function produced() {
    return length + (runStart === -1 ? 0 : runEnd - runStart);
  }
  var pendingHigh = -1; // a high surrogate waiting for a low one (dropped units may lie between them)
  var pendingAt = -1;
  for (; index < n; index++) {
    var c = value.charCodeAt(index);
    if (c === 0x09 || c === 0x0A || c === 0x0D) {
      continue;
    }
    var unit = chars.toLowerCaseChar(c);
    if (pendingHigh !== -1) {
      if (unit >= 0xDC00 && unit <= 0xDFFF) {
        var lower = chars.lowerRootCodePoint(((pendingHigh - 0xD800) << 10) + (unit - 0xDC00) + 0x10000);
        if (pendingAt === index - 1 && lower.length === 2 && lower.charCodeAt(0) === value.charCodeAt(pendingAt)
            && lower.charCodeAt(1) === c) {
          same(pendingAt);
          same(index);
        } else {
          text(lower);
        }
        pendingHigh = -1;
        if (produced() >= need) {
          break;
        }
        continue;
      }
      same(pendingAt); // Character.toLowerCase leaves a surrogate unchanged
      pendingHigh = -1;
    }
    if (unit >= 0xD800 && unit <= 0xDBFF) {
      pendingHigh = unit;
      pendingAt = index;
      continue;
    }
    var before = produced();
    if (unit < 0x80) {
      if (unit === c) {
        same(index);
      } else {
        text(String.fromCharCode(unit));
      }
    } else {
      var mapped = chars.lowerRootCodePoint(unit);
      if (mapped.length === 1 && mapped.charCodeAt(0) === c) {
        same(index);
      } else {
        text(mapped);
      }
    }
    if (produced() >= need || (toDataEnd && before >= DATA_PREFIX.length && (unit === 0x3B || unit === 0x2C))) {
      break;
    }
  }
  if (pendingHigh !== -1) {
    same(pendingAt);
  }
  flush();
  return pieces.length === 1 ? pieces[0] : pieces.join('');
}

// The event-handler names as a trie over their (lower-case ASCII) units: node[unit] is the next node, node.end marks
// a name. The list is fixed (the bundled data file), so one trie serves every detector; it is never modified.
var EMPTY_TRIE = Object.freeze({});
var sharedTrie = null;

function handlerNameTrie() {
  if (sharedTrie === null) {
    var root = {};
    EventHandlerNameList.load().forEach(function (name) {
      var node = root;
      for (var k = 0; k < name.length; k++) {
        var u = name.charCodeAt(k);
        node = node[u] || (node[u] = {});
      }
      node.end = true;
    });
    sharedTrie = root;
  }
  return sharedTrie;
}

class ContentDetector {
  /** @param {ContentPolicy|object} policy a ContentPolicy, or options for one */
  constructor(policy) {
    if (policy === null || policy === undefined) {
      throw new TypeError('policy');
    }
    var p = policy instanceof ContentPolicy ? policy : new ContentPolicy(policy);
    var names = p.eventHandler ? EventHandlerNameList.load() : [];
    var longestName = 0;
    names.forEach(function (name) {
      longestName = Math.max(longestName, name.length);
    });
    var trie = p.eventHandler ? handlerNameTrie() : EMPTY_TRIE;
    var schemePrefixes = p.deniedSchemes.map(function (scheme) {
      return scheme + ':';
    });
    var need = DATA_PREFIX.length;
    schemePrefixes.forEach(function (prefix) {
      need = Math.max(need, prefix.length);
    });
    var controls = new Uint8Array(32);
    p.disallowedControls.forEach(function (control) {
      controls[control] = 1;
    });
    this.policy = p;
    Object.defineProperties(this, {
      handlerTrie: { value: trie },
      longestHandlerName: { value: longestName },
      schemePrefixes: { value: schemePrefixes },
      mediaTypes: { value: new Set(p.deniedDataMediaTypes) },
      urlPrefixNeed: { value: need },
      controls: { value: controls }
    });
    Object.freeze(this);
  }

  /**
   * The first matching rule ('R1', 'R2', 'R3' or 'R4'), checked in that order over every variant, or null.
   * @param {string} value
   */
  detect(value) {
    if (typeof value !== 'string') {
      throw new TypeError('value must be a string');
    }
    var p = this.policy;
    if (!p.normalizeNfkc && hasNoSentinel(value)) {
      return null;
    }
    var variants = this.variants(value);
    var i;
    if (p.markupStart) {
      for (i = 0; i < variants.length; i++) {
        if (hasMarkupStart(variants[i])) {
          return 'R1';
        }
      }
    }
    if (p.urlScheme) {
      for (i = 0; i < variants.length; i++) {
        if (this.hasDeniedUrl(variants[i])) {
          return 'R2';
        }
      }
    }
    if (p.eventHandler) {
      for (i = 0; i < variants.length; i++) {
        if (this.hasEventHandlerAssignment(variants[i])) {
          return 'R3';
        }
      }
    }
    if (p.disallowedControls.length !== 0) {
      for (i = 0; i < variants.length; i++) {
        if (this.hasDisallowedControl(variants[i])) {
          return 'R4';
        }
      }
    }
    return null;
  }

  /**
   * The distinct, insertion-ordered variants Java checks: the original, then up to decodeRounds rounds of entity
   * decoding, percent decoding and (when enabled) NFKC applied to the previous round's new variants, capped at 40
   * variants and at max(len + 64, len * 18) units each.
   */
  variants(original) {
    var p = this.policy;
    if (!p.normalizeNfkc && !(decoder.mayDecodeEntities(original) || decoder.mayDecodePercent(original))) {
      return [original];
    }
    var all = new Set();
    var list = [original];
    all.add(original);
    if (p.normalizeNfkc && p.decodeRounds === 0) {
      var normalized = nfkc.nfkcJ8(original);
      if (!all.has(normalized)) {
        list.push(normalized);
      }
      return list;
    }
    var frontier = [original];
    var limit = growthLimit(original.length);
    var add = function (next, candidate) {
      if (candidate.length <= limit && all.size < MAX_VARIANTS && !all.has(candidate)) {
        all.add(candidate);
        list.push(candidate);
        next.push(candidate);
      }
    };
    for (var round = 0; round < p.decodeRounds && all.size < MAX_VARIANTS; round++) {
      var next = [];
      for (var k = 0; k < frontier.length; k++) {
        var candidate = frontier[k];
        // A decoder's result can only be added while fewer than MAX_VARIANTS exist; the decoders have no side
        // effects, so skipping them past that point gives the same variants.
        if (all.size < MAX_VARIANTS && decoder.mayDecodeEntities(candidate)) {
          add(next, decoder.decodeEntities(candidate));
        }
        if (all.size < MAX_VARIANTS && decoder.mayDecodePercent(candidate)) {
          add(next, decoder.decodePercent(candidate));
        }
        if (all.size < MAX_VARIANTS && p.normalizeNfkc) {
          add(next, nfkc.nfkcJ8(candidate));
        }
        if (all.size >= MAX_VARIANTS) {
          break;
        }
      }
      if (next.length === 0) {
        break;
      }
      frontier = next;
    }
    return list;
  }

  /** R2: a denied scheme, or a data: URL with a denied media type, after URL canonicalization. */
  hasDeniedUrl(value) {
    var schemes = this.schemePrefixes;
    var mediaTypes = this.mediaTypes;
    if (schemes.length === 0 && mediaTypes.size === 0) {
      return false;
    }
    var prefix = canonicalUrlPrefix(value, this.urlPrefixNeed, false);
    for (var i = 0; i < schemes.length; i++) {
      if (prefix.startsWith(schemes[i])) {
        return true;
      }
    }
    if (!prefix.startsWith(DATA_PREFIX) || mediaTypes.size === 0) {
      return false;
    }
    var canonical = canonicalUrlPrefix(value, Infinity, true);
    var start = DATA_PREFIX.length;
    while (start < canonical.length && isLeadingUrlSpaceOrControl(canonical.charCodeAt(start))) {
      start++;
    }
    var end = start;
    while (end < canonical.length && canonical.charCodeAt(end) !== 0x3B && canonical.charCodeAt(end) !== 0x2C) {
      end++;
    }
    return mediaTypes.has(chars.javaTrim(canonical.slice(start, end)));
  }

  /** R3: a known event-handler name starting a word, then optional whitespace, then '='. */
  hasEventHandlerAssignment(value) {
    var longest = this.longestHandlerName;
    var n = value.length;
    for (var index = 0; index + 1 < n; index++) {
      var c = value.charCodeAt(index);
      if (c !== 0x6F && c !== 0x4F) {
        continue;
      }
      var d = value.charCodeAt(index + 1);
      if ((d !== 0x6E && d !== 0x4E) || (index > 0 && isAsciiWord(value.charCodeAt(index - 1)))) {
        continue;
      }
      var after = index + 2;
      while (after < n && isAsciiWord(value.charCodeAt(after))) {
        after++;
      }
      // The candidate is ASCII, so ASCII lower-casing equals Locale.ROOT; names longer than any known one never match.
      if (after - index > longest || !this.isHandlerName(value, index, after)) {
        index = after - 1;
        continue;
      }
      var equals = after;
      while (equals < n && chars.isWhitespace(value.charCodeAt(equals))) {
        equals++;
      }
      if (equals < n && value.charCodeAt(equals) === 0x3D) {
        return true;
      }
    }
    return false;
  }

  /** Whether value[start, end) (ASCII word units) is an event-handler name, ignoring ASCII case. */
  isHandlerName(value, start, end) {
    var node = this.handlerTrie;
    for (var k = start; k < end; k++) {
      var u = value.charCodeAt(k);
      node = node[u >= 0x41 && u <= 0x5A ? u + 0x20 : u];
      if (node === undefined) {
        return false;
      }
    }
    return node.end === true;
  }

  /** R4: some code point is in disallowedControls. */
  hasDisallowedControl(value) {
    var controls = this.controls;
    for (var offset = 0; offset < value.length;) {
      var cp = value.codePointAt(offset);
      if (cp < 32 && controls[cp] === 1) {
        return true;
      }
      offset += cp > 0xFFFF ? 2 : 1;
    }
    return false;
  }
}

ContentDetector.MAX_VARIANTS = MAX_VARIANTS;
ContentDetector.MAX_NORMALIZATION_EXPANSION = MAX_NORMALIZATION_EXPANSION;
ContentDetector.hasNoSentinel = hasNoSentinel;
ContentDetector.hasMarkupStart = hasMarkupStart;
ContentDetector.canonicalizeUrlPrefix = function (value) {
  return canonicalUrlPrefix(value, Infinity, false);
};

module.exports = ContentDetector;
