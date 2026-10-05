'use strict';

// Zero-dependency test runner: loads every test/*.test.js (sorted), runs the registered tests in order and exits
// non-zero on any failure. Optional arguments are substrings that select test files.

var fs = require('fs');
var path = require('path');
var harness = require('./harness');

var filters = process.argv.slice(2);
var files = fs.readdirSync(__dirname).filter(function (name) {
  return name.length > 8 && name.slice(-8) === '.test.js';
}).filter(function (name) {
  return filters.length === 0 || filters.some(function (f) {
    return name.indexOf(f) !== -1;
  });
}).sort();

files.forEach(function (name) {
  harness.setFile(name);
  require(path.join(__dirname, name));
});

var passed = 0;
var failed = 0;
var skipped = 0;
var failures = [];
var started = Date.now();
var perFile = {};

function record(t, error, ms) {
  var stats = perFile[t.file] || (perFile[t.file] = { passed: 0, failed: 0, skipped: 0, ms: 0 });
  stats.ms += ms;
  if (error === null) {
    passed++;
    stats.passed++;
  } else if (error instanceof harness.SkipTest) {
    skipped++;
    stats.skipped++;
    console.log('  skip  ' + t.file + ' > ' + t.name + ' (' + error.reason + ')');
  } else {
    failed++;
    stats.failed++;
    failures.push({ t: t, error: error });
    console.log('  FAIL  ' + t.file + ' > ' + t.name);
  }
}

function finish() {
  Object.keys(perFile).sort().forEach(function (file) {
    var s = perFile[file];
    console.log((s.failed ? 'FAIL ' : 'ok   ') + file + ': ' + s.passed + ' passed' + (s.failed ? ', ' + s.failed + ' failed' : '')
        + (s.skipped ? ', ' + s.skipped + ' skipped' : '') + ' (' + s.ms + ' ms)');
  });
  failures.forEach(function (f) {
    console.log('\n' + f.t.file + ' > ' + f.t.name + '\n' + (f.error && f.error.stack ? f.error.stack : String(f.error)));
  });
  console.log('\nnode ' + process.version + ': ' + passed + ' passed, ' + failed + ' failed, ' + skipped + ' skipped in '
      + (Date.now() - started) + ' ms');
  process.exitCode = failed === 0 && passed > 0 ? 0 : 1;
}

function run(index) {
  while (index < harness.tests.length) {
    var t = harness.tests[index];
    var t0 = Date.now();
    var result;
    try {
      result = t.fn();
    } catch (e) {
      record(t, e, Date.now() - t0);
      index++;
      continue;
    }
    if (result && typeof result.then === 'function') {
      var next = index + 1;
      result.then(function () {
        record(t, null, Date.now() - t0);
        run(next);
      }, function (e) {
        record(t, e === undefined ? new Error('rejected') : e, Date.now() - t0);
        run(next);
      });
      return;
    }
    record(t, null, Date.now() - t0);
    index++;
  }
  finish();
}

run(0);
