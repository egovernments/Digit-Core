'use strict';

// Startup lines (CoverageReport): the effective configuration, one coverage line per route entry, and the reason
// (INFO) or a missing reason (WARN) for every exclusion: enabled:false, structured:false or skip paths.

var defaults = require('./defaults');
var resolve = require('./resolve');
var safeText = require('../web/auditLogger').safeText;

/** [{ level: 'info'|'warn', line }] for an enabled instance. */
function startupLines(resolved) {
  var cfg = resolved.internal;
  var lines = [];
  lines.push({
    level: 'info',
    line: 'request_validation_config enabled=true activation=' + cfg.activation + ' mode=' + cfg.mode
        + ' structuredDefault=' + cfg.structuredDefault + ' jar_sha256=' + defaults.JAR_SHA256
  });
  var anyEnabled = false;
  resolved.entries.forEach(function (entry) {
    var policy = resolved.resolver.forEntry(entry);
    if (entry.source === 'route' && entry.enabled !== false) {
      anyEnabled = true;
    }
    lines.push({
      level: 'info',
      line: 'request_validation_coverage handler=' + safeText(entry.name) + ' enabled=' + policy.enabled + ' mode='
          + policy.mode
    });
    var exclusion = entry.enabled === false || entry.structured === false || entry.skipPaths.length > 0;
    if (exclusion) {
      if (resolve.isBlankReason(entry.reason)) {
        lines.push({ level: 'warn', line: 'request_validation_exclusion_missing_reason handler=' + safeText(entry.name) });
      } else {
        lines.push({
          level: 'info',
          line: 'request_validation_exclusion handler=' + safeText(entry.name) + ' reason=' + safeText(entry.reason)
        });
      }
    }
  });
  if (cfg.activation === 'ANNOTATED' && !anyEnabled) {
    lines.push({ level: 'warn', line: 'request_validation_nothing_validated' });
  }
  return lines;
}

module.exports = {
  startupLines: startupLines
};
