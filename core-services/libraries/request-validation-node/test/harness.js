'use strict';

// Minimal test registry shared by test/run.js and every test/*.test.js file.

var fs = require('fs');
var path = require('path');
var crypto = require('crypto');

var tests = [];
var currentFile = null;

function SkipTest(reason) {
  this.reason = reason;
}

/** Registers a test. `fn` may return a promise. */
function test(name, fn) {
  tests.push({ file: currentFile, name: name, fn: fn });
}

/** Ends the current test as skipped. */
function skip(reason) {
  throw new SkipTest(reason);
}

function setFile(file) {
  currentFile = file;
}

var ROOT = path.join(__dirname, '..');

function readFixture(name) {
  return fs.readFileSync(path.join(__dirname, 'fixtures', name), 'utf8');
}

function readJsonFixture(name) {
  return JSON.parse(readFixture(name));
}

function readJsonLinesFixture(name) {
  return readFixture(name).split('\n').filter(function (line) {
    return line.length !== 0;
  }).map(function (line) {
    return JSON.parse(line);
  });
}

function sha256(data) {
  return crypto.createHash('sha256').update(data).digest('hex');
}

/** Lower-case hex of each UTF-16 unit, unpadded, joined by '.', as the Java dump tools print strings. */
function unitsHex(value) {
  var out = '';
  for (var i = 0; i < value.length; i++) {
    out += (i ? '.' : '') + value.charCodeAt(i).toString(16);
  }
  return out;
}

function lib(relative) {
  return require(path.join(ROOT, 'lib', 'core', relative));
}

module.exports = {
  test: test,
  skip: skip,
  SkipTest: SkipTest,
  tests: tests,
  setFile: setFile,
  ROOT: ROOT,
  readFixture: readFixture,
  readJsonFixture: readJsonFixture,
  readJsonLinesFixture: readJsonLinesFixture,
  sha256: sha256,
  unitsHex: unitsHex,
  lib: lib
};
