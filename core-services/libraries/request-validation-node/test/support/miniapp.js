'use strict';

// A small in-process stand-in for Express + body-parser, used so the web tests run on every Node version with no
// dependency and no socket. It reproduces the parts of their behaviour the adapter relies on:
// - Express: middlewares in order; an error skips to handlers of arity 4; a thrown error becomes next(err); routes
//   with :params set req.params; req.originalUrl.
// - body-parser 1.x: skips without a body or when already parsed; checks the type and the charset (415); reads the
//   whole stream; calls verify(req, res, buf, charset); a throw from verify is passed on the way http-errors'
//   createError(403, err, { body, type }) does (status kept, body/type/expose added); sets req._body and req.body.

var PassThrough = require('stream').PassThrough;
var querystring = require('querystring');

function hasBody(req) {
  return req.headers['transfer-encoding'] !== undefined || !isNaN(req.headers['content-length']);
}

function mediaTypeOf(req) {
  var value = req.headers['content-type'];
  if (typeof value !== 'string') {
    return null;
  }
  var semi = value.indexOf(';');
  return (semi === -1 ? value : value.slice(0, semi)).trim().toLowerCase();
}

function charsetOf(req) {
  var value = req.headers['content-type'] || '';
  var m = /;\s*charset\s*=\s*"?([^";\s]+)"?/i.exec(value);
  return m ? m[1].toLowerCase() : undefined;
}

function httpError(status, message, props) {
  var err = new Error(message);
  err.status = err.statusCode = status;
  err.expose = status < 500;
  Object.keys(props || {}).forEach(function (k) {
    err[k] = props[k];
  });
  return err;
}

// http-errors createError(403, err, { body, type }) applied to an error thrown by a verify hook.
function wrapVerifyError(err, body) {
  if (err === null || typeof err !== 'object') {
    return httpError(403, String(err), { body: body, type: 'entity.verify.failed' });
  }
  var status = err.status || err.statusCode || 403;
  err.expose = status < 500;
  err.status = err.statusCode = status;
  err.body = body;
  err.type = err.type || 'entity.verify.failed';
  return err;
}

function readAll(req, limit, callback) {
  var chunks = [];
  var size = 0;
  var done = false;
  req.on('data', function (chunk) {
    if (done) {
      return;
    }
    size += chunk.length;
    if (size > limit) {
      done = true;
      callback(httpError(413, 'request entity too large', { type: 'entity.too.large', limit: limit }));
      return;
    }
    chunks.push(chunk);
  });
  req.on('end', function () {
    if (!done) {
      done = true;
      callback(null, Buffer.concat(chunks));
    }
  });
  req.on('aborted', function () {
    if (!done) {
      done = true;
      callback(httpError(400, 'request aborted', { type: 'request.aborted' }));
    }
  });
}

/**
 * options: verify, limit (default 10 MiB), parse ('raw' | 'text', default by kind), type (function(req) -> boolean),
 * parameterLimit (urlencoded)
 */
function bodyParser(kind, options) {
  var opts = options || {};
  var limit = opts.limit || 10 * 1024 * 1024;
  var typeCheck = opts.type || (kind === 'json'
    ? function (req) {
      var t = mediaTypeOf(req);
      return t === 'application/json' || (t !== null && t.indexOf('application/') === 0 && t.slice(-5) === '+json');
    }
    : function (req) {
      return mediaTypeOf(req) === 'application/x-www-form-urlencoded';
    });
  return function parser(req, res, next) {
    if (req._body) {
      next();
      return;
    }
    req.body = req.body || {};
    if (!hasBody(req) || !typeCheck(req)) {
      next();
      return;
    }
    var charset = charsetOf(req) || 'utf-8';
    if (kind === 'json' ? charset.slice(0, 4) !== 'utf-' : charset !== 'utf-8' && charset !== 'iso-8859-1') {
      next(httpError(415, 'unsupported charset', { type: 'charset.unsupported', charset: charset }));
      return;
    }
    req._body = true;
    readAll(req, limit, function (err, buf) {
      if (err) {
        next(err);
        return;
      }
      if (opts.verify) {
        try {
          opts.verify(req, res, buf, charset);
        } catch (e) {
          next(wrapVerifyError(e, buf));
          return;
        }
      }
      if (kind === 'urlencoded' && opts.parameterLimit !== undefined) {
        // body-parser counts '&' after the verify hook ran and answers 413 at parameterLimit
        var text = buf.toString('latin1');
        var count = 0;
        for (var at = text.indexOf('&'); at !== -1; at = text.indexOf('&', at + 1)) {
          if (++count === opts.parameterLimit) {
            next(httpError(413, 'too many parameters', { type: 'parameters.too.many' }));
            return;
          }
        }
      }
      try {
        if (opts.parse === 'raw') {
          req.body = buf;
        } else if (opts.parse === 'text') {
          req.body = buf.toString(charset === 'iso-8859-1' ? 'latin1' : 'utf8');
        } else if (kind === 'json') {
          req.body = buf.length === 0 ? {} : JSON.parse(buf.toString('utf8'));
        } else {
          req.body = querystring.parse(buf.toString(charset === 'iso-8859-1' ? 'latin1' : 'utf8'));
        }
      } catch (e) {
        next(httpError(400, 'parse failed', { type: 'entity.parse.failed' }));
        return;
      }
      next();
    });
  };
}

function compileRoute(pattern) {
  var names = [];
  var parts = pattern.split('/').map(function (segment) {
    if (segment.charAt(0) === ':') {
      names.push(segment.slice(1));
      return '([^/]+?)';
    }
    return segment.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  });
  return { regexp: new RegExp('^' + parts.join('/') + '\\/?$', 'i'), names: names };
}

function App() {
  this.stack = [];
}

App.prototype.use = function () {
  var self = this;
  Array.prototype.slice.call(arguments).forEach(function add(fn) {
    if (Array.isArray(fn)) {
      fn.forEach(add);
    } else {
      self.stack.push({ fn: fn });
    }
  });
  return this;
};

App.prototype.route = function (method, pattern) {
  var fns = Array.prototype.slice.call(arguments, 2);
  this.stack.push({ method: method.toUpperCase(), route: compileRoute(pattern), fns: fns });
  return this;
};

App.prototype.handle = function (req, res, finalHandler) {
  var stack = this.stack;
  var index = 0;
  function call(fn, err, next) {
    try {
      if (err) {
        if (fn.length === 4) {
          fn(err, req, res, next);
        } else {
          next(err);
        }
      } else if (fn.length < 4) {
        fn(req, res, next);
      } else {
        next();
      }
    } catch (e) {
      next(e);
    }
  }
  function next(err) {
    if (index >= stack.length) {
      finalHandler(err);
      return;
    }
    var layer = stack[index++];
    if (!layer.route) {
      call(layer.fn, err, next);
      return;
    }
    var path = req.url.split('?')[0];
    var m = layer.route.regexp.exec(path);
    if (!m || (layer.method !== 'ALL' && layer.method !== req.method && !(layer.method === 'GET' && req.method === 'HEAD'))) {
      next(err);
      return;
    }
    req.params = {};
    layer.route.names.forEach(function (name, i) {
      req.params[name] = decodeURIComponent(m[i + 1]);
    });
    var i = 0;
    (function routeNext(e) {
      if (e === 'route') {
        next();
        return;
      }
      if (i >= layer.fns.length) {
        next(e);
        return;
      }
      call(layer.fns[i++], e, routeNext);
    })(err);
  }
  next();
};

/** A request stream with headers; `body` (string or Buffer) is written and ended asynchronously. */
function createRequest(options) {
  var req = new PassThrough();
  req.method = options.method || 'GET';
  req.url = options.url || '/';
  req.originalUrl = req.url;
  req.headers = {};
  Object.keys(options.headers || {}).forEach(function (k) {
    req.headers[k.toLowerCase()] = options.headers[k];
  });
  var body = options.body === undefined || options.body === null ? null
    : Buffer.isBuffer(options.body) ? options.body : Buffer.from(options.body, options.bodyEncoding || 'utf8');
  if (body !== null && req.headers['content-length'] === undefined && req.headers['transfer-encoding'] === undefined) {
    req.headers['content-length'] = String(body.length);
  }
  setImmediate(function () {
    if (options.abort) {
      if (body) {
        req.write(body.slice(0, Math.floor(body.length / 2)));
      }
      req.emit('aborted');
      return;
    }
    if (body) {
      req.end(body);
    } else {
      req.end();
    }
  });
  return req;
}

function createResponse(onEnd) {
  var res = {
    statusCode: 200,
    headers: {},
    headersSent: false,
    finished: false,
    body: null,
    setHeader: function (name, value) {
      if (res.headersSent) {
        throw new Error('headers already sent');
      }
      res.headers[name.toLowerCase()] = value;
    },
    getHeader: function (name) {
      return res.headers[name.toLowerCase()];
    },
    end: function (data) {
      if (res.finished) {
        throw new Error('response already ended');
      }
      res.headersSent = true;
      res.finished = true;
      res.body = data === undefined ? Buffer.alloc(0) : Buffer.isBuffer(data) ? data : Buffer.from(String(data));
      onEnd();
    }
  };
  return res;
}

/**
 * Runs one request through the app. Resolves with { status, headers, body (string), req, handled, error }.
 * `handled` is true when a route handler answered; `error` is what reached the final handler.
 */
function request(app, options) {
  return new Promise(function (resolve) {
    var req = createRequest(options);
    var outcome = { handled: false, error: null };
    var res = createResponse(function () {
      setImmediate(function () {
        resolve({ status: res.statusCode, headers: res.headers, body: res.body.toString('utf8'), raw: res.body, req: req,
          handled: outcome.handled, error: outcome.error });
      });
    });
    res.markHandled = function () {
      outcome.handled = true;
    };
    app.handle(req, res, function (err) {
      outcome.error = err || null;
      if (res.finished) {
        return;
      }
      res.statusCode = err ? (err.status || 500) : 404;
      res.end(err ? 'host-error ' + (err.type || err.name) : 'not found');
    });
  });
}

/**
 * A multipart/form-data parser for the tests (the role multer plays): reads the stream and puts every part without
 * a filename on req.body (repeated names become arrays); parts with a filename go to req.files.
 */
function multipartParser() {
  return function (req, res, next) {
    var type = req.headers['content-type'] || '';
    var m = /^multipart\/form-data;\s*boundary=([^;]+)$/i.exec(type);
    if (!m) {
      next();
      return;
    }
    readAll(req, 10 * 1024 * 1024, function (err, buf) {
      if (err) {
        next(err);
        return;
      }
      var body = Object.create(null);
      var files = [];
      buf.toString('utf8').split('--' + m[1]).forEach(function (part) {
        var sep = part.indexOf('\r\n\r\n');
        if (sep === -1) {
          return;
        }
        var head = part.slice(0, sep);
        var value = part.slice(sep + 4).replace(/\r\n$/, '');
        var name = /name="([^"]*)"/.exec(head);
        if (!name) {
          return;
        }
        if (/filename="/.test(head)) {
          files.push({ fieldname: name[1], size: value.length });
        } else if (body[name[1]] === undefined) {
          body[name[1]] = value;
        } else {
          body[name[1]] = [].concat(body[name[1]], value);
        }
      });
      req.body = body;
      req.files = files;
      next();
    });
  };
}

/** A route handler that answers 200 with what it received. */
function echo(req, res) {
  res.markHandled();
  res.statusCode = 200;
  res.setHeader('Content-Type', 'application/json');
  res.end(JSON.stringify({ ok: true, body: Buffer.isBuffer(req.body) ? req.body.toString('base64') : req.body, params: req.params,
    query: req.query }));
}

/** Records logger lines by level. */
function recordingLogger() {
  var lines = [];
  return {
    lines: lines,
    warn: function (line) {
      lines.push({ level: 'warn', line: line });
    },
    info: function (line) {
      lines.push({ level: 'info', line: line });
    },
    error: function (line) {
      lines.push({ level: 'error', line: line });
    },
    findings: function () {
      return lines.filter(function (l) {
        return l.line.indexOf('request_validation mode=') === 0;
      }).map(function (l) {
        var f = {};
        l.line.split(' ').slice(1).forEach(function (kv) {
          var eq = kv.indexOf('=');
          f[kv.slice(0, eq)] = kv.slice(eq + 1);
        });
        return f;
      });
    },
    clear: function () {
      lines.length = 0;
    }
  };
}

/**
 * The standard service wiring: beforeParsers, JSON and form parsers with the verify hooks, afterParsers, then a
 * POST /json echo route, a GET /query route, a /path/:id route with pathParams and a catch-all echo.
 */
function serviceApp(rv, options) {
  var opts = options || {};
  var app = new App();
  (opts.before || []).forEach(function (fn) {
    app.use(fn);
  });
  app.use(rv.beforeParsers);
  app.use(bodyParser('json', { verify: rv.jsonVerify, parse: opts.rawJson ? 'raw' : 'json', limit: opts.jsonLimit }));
  app.use(bodyParser('urlencoded', { verify: rv.formVerify }));
  app.use(rv.afterParsers);
  app.route('ALL', '/path/:id', rv.pathParams, echo);
  app.route('ALL', '/two/:a/:b', rv.pathParams, echo);
  app.use(echo);
  return app;
}

module.exports = {
  App: App,
  bodyParser: bodyParser,
  multipartParser: multipartParser,
  request: request,
  echo: echo,
  recordingLogger: recordingLogger,
  serviceApp: serviceApp,
  createRequest: createRequest,
  createResponse: createResponse
};
