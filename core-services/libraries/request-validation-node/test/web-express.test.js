'use strict';

// End-to-end checks on real Express and body-parser (and multer when installed). Skipped unless RV_TEST_EXPRESS lists
// node_modules directories that contain express (comma-separated); RV_TEST_PORT sets the port (default: any free
// port). Every case checks the answer, the log and what the handler received.

var assert = require('assert');
var net = require('net');
var path = require('path');
var h = require('./harness');
var test = h.test;
var rv = require(path.join(h.ROOT, 'index.js'));

var INSTALLS = (process.env.RV_TEST_EXPRESS || '').split(',').filter(function (dir) {
  return dir.length > 0;
});
var PORT = Number(process.env.RV_TEST_PORT || 0);

function send(port, head, body) {
  return new Promise(function (resolve, reject) {
    var chunks = [];
    var socket = net.connect(port, '127.0.0.1', function () {
      socket.write(Buffer.from(head, 'latin1'));
      if (body) {
        socket.write(Buffer.from(body, 'latin1'));
      }
    });
    socket.on('data', function (d) {
      chunks.push(d);
    });
    socket.on('error', reject);
    socket.on('close', function () {
      var text = Buffer.concat(chunks).toString('utf8');
      var sep = text.indexOf('\r\n\r\n');
      resolve({ status: Number(text.slice(9, 12)), body: sep === -1 ? '' : text.slice(sep + 4) });
    });
  });
}

function request(port, method, target, contentType, body) {
  var head = method + ' ' + target + ' HTTP/1.1\r\nHost: 127.0.0.1\r\nConnection: close\r\n';
  if (contentType) {
    head += 'Content-Type: ' + contentType + '\r\n';
  }
  if (body !== null && body !== undefined) {
    head += 'Content-Length: ' + Buffer.byteLength(body, 'latin1') + '\r\n';
  }
  return send(port, head + '\r\n', body);
}

function stack(dir) {
  var express = require(path.join(dir, 'express'));
  var major = Number(require(path.join(dir, 'express', 'package.json')).version.split('.')[0]);
  var bodyParser = require(path.join(dir, 'body-parser'));
  var bodyParserMajor = Number(require(path.join(dir, 'body-parser', 'package.json')).version.split('.')[0]);
  var multer = null;
  try {
    multer = require(path.join(dir, 'multer'));
  } catch (e) {
    multer = null;
  }
  return { express: express, major: major, bodyParser: bodyParser, bodyParserMajor: bodyParserMajor, multer: multer };
}

function app(s, options, extended) {
  var lines = [];
  var r = rv.createRequestValidation(Object.assign({ env: false, enabled: true, structuredDefault: true,
    logger: { warn: function (l) {
      lines.push(l);
    }, info: function () {} } }, options));
  var a = s.express();
  a.use(r.beforeParsers);
  a.use(s.bodyParser.json({ verify: r.jsonVerify }));
  a.use(s.bodyParser.urlencoded({ extended: extended !== false, verify: r.formVerify }));
  a.use(r.afterParsers);
  function echo(req, res) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify({ reached: true, query: req.query, body: req.body }));
  }
  if (s.multer) {
    a.post('/upload', s.multer().none(), r.multipartFields, echo);
  }
  a.post('/v1/_create', echo);
  a.all('/x', echo);
  a.use(function (err, req, res, next) { // eslint-disable-line no-unused-vars
    res.statusCode = err.status || 500;
    res.end(String(err.type));
  });
  return { app: a, lines: lines };
}

function run(s, options, extended, method, target, contentType, body) {
  var built = app(s, options, extended);
  return new Promise(function (resolve, reject) {
    var server = built.app.listen(PORT, '127.0.0.1', function () {
      request(server.address().port, method, target, contentType, body).then(function (r) {
        server.close(function () {
          r.lines = built.lines;
          r.reached = r.body.indexOf('"reached":true') !== -1;
          r.echo = r.reached ? JSON.parse(r.body) : null;
          resolve(r);
        });
      }, reject);
    });
  });
}

function rejected(r, label) {
  assert.strictEqual(r.status, 400, label + ' ' + r.body);
  assert.ok(!r.reached, label);
  assert.ok(r.lines.length > 0, label);
}

function receivedWithout(r, container, key, label) {
  assert.strictEqual(r.status, 200, label);
  assert.ok(r.reached, label);
  assert.ok(!Object.prototype.hasOwnProperty.call(r.echo[container] || {}, key), label + ' ' + r.body);
  assert.strictEqual(r.body.indexOf('<script'), -1, label);
}

var ANNOTATED = { activation: 'ANNOTATED', mode: 'ENFORCE', routes: [{ path: '/v1/_create', method: 'POST' }] };
var ALL = { activation: 'ALL', mode: 'ENFORCE' };
var JSON_CT = 'application/json';
var FORM_CT = 'application/x-www-form-urlencoded';

test('real Express: route policies, query and form views, multipart and form limits', function () {
  if (INSTALLS.length === 0) {
    h.skip('RV_TEST_EXPRESS is not set');
  }
  var steps = [];
  INSTALLS.forEach(function (dir) {
    var s = stack(dir);
    var tag = 'express ' + s.major + ' (' + dir + ')';
    ['/v1/_create', '/v1/_create#x', '/v1\\_create#', '/V1/_CREATE#x'].forEach(function (target) {
      steps.push(function () {
        return run(s, ANNOTATED, true, 'POST', target, JSON_CT, '{"a":"<script>alert(1)</script>"}').then(function (r) {
          rejected(r, tag + ' ' + target);
        });
      });
    });
    steps.push(function () {
      return run(s, ALL, true, 'GET', '/x?=%3Cscript%3E', null, null).then(function (r) {
        if (s.major >= 5) {
          rejected(r, tag + ' empty name, querystring query parser');
        } else {
          receivedWithout(r, 'query', '', tag + ' empty name, qs query parser');
        }
      });
    });
    steps.push(function () {
      return run(s, ALL, false, 'POST', '/x', FORM_CT, '=%3Cscript%3E').then(function (r) {
        if (s.bodyParserMajor < 2) {
          rejected(r, tag + ' empty name, querystring form parser');
        } else {
          receivedWithout(r, 'body', '', tag + ' empty name, qs form parser');
        }
      });
    });
    steps.push(function () {
      return run(s, ALL, true, 'GET', '/x?link[x=y]=javascript:alert(1)', null, null).then(function (r) {
        rejected(r, tag + ' qs ]= split');
      });
    });
    steps.push(function () {
      var pairs = [];
      for (var i = 0; i < 10010; i++) {
        pairs.push('p' + i + '=' + (i === 10005 ? '%3Cscript%3E' : '1'));
      }
      return run(s, ALL, true, 'POST', '/x', FORM_CT, pairs.join('&')).then(function (r) {
        assert.strictEqual(r.status, 413, tag + ' parameterLimit');
        assert.deepStrictEqual(r.lines, [], tag + ' no inspection of parameters the parser rejected');
      });
    });
    if (s.multer) {
      var part = '--B0\r\nContent-Disposition: form-data; name="note"\r\n\r\n<script>alert(1)</script>\r\n--B0--\r\n';
      steps.push(function () {
        return run(s, ALL, true, 'POST', '/upload', 'multipart/form-data; boundary=B0', part).then(function (r) {
          rejected(r, tag + ' multipart field');
        });
      });
      steps.push(function () {
        return run(s, ALL, true, 'POST', '/upload', 'multipart/form-data; boundary=B0', part.replace('<script>alert(1)</script>', 'hello'))
            .then(function (r) {
              assert.strictEqual(r.status, 200, tag);
              assert.deepStrictEqual(r.echo.body, { note: 'hello' }, tag);
            });
      });
    }
  });
  return steps.reduce(function (p, step) {
    return p.then(step);
  }, Promise.resolve());
});
