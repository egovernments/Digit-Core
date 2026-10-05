'use strict';

// Per-request state, stored on the request under a Symbol owned by one package instance: non-enumerable, so
// Object.keys(req), JSON serialisation and the request's own properties are unchanged.

var SafeLocationFormatter = require('../core/SafeLocationFormatter');
var requestTarget = require('./requestTarget');

var rawTarget = requestTarget.rawTarget;

/**
 * @param {function(string, string): object} resolvePolicy (method, pathname) => effective policy
 */
function StateStore(resolvePolicy) {
  this.key = Symbol('request-validation');
  this.resolvePolicy = resolvePolicy;
}

StateStore.prototype.get = function (req) {
  return req[this.key];
};

/** The request's state, created (and its policy resolved) on first use. */
StateStore.prototype.ensure = function (req) {
  var state = req[this.key];
  if (state !== undefined) {
    return state;
  }
  var target = rawTarget(req);
  var routed = requestTarget.routedTarget(req, target);
  var pathname = routed.pathname;
  var method = typeof req.method === 'string' ? req.method : '';
  var policy = this.resolvePolicy(method, pathname);
  state = {
    policy: policy,
    active: policy.enabled === true,
    // the route name, else the path with every segment that is not a safe name written as '*' (no request text)
    handler: policy.name || SafeLocationFormatter.sanitizeLocation(pathname),
    method: method,
    target: target,
    appQuery: routed.query,
    count: 0,
    incomplete: false,
    scalarsDone: false,
    scalarStopped: false,
    formDone: false,
    budget: [0, 0],
    scalarNames: null,
    pendingForm: null,
    formEmptyNames: null,
    multipartDone: false,
    bodyDone: false,
    t0: null,
    jsonHookSeen: false,
    formHookSeen: false,
    internalErrorLogged: false,
    logContext: undefined,
    pathSeen: null
  };
  Object.defineProperty(req, this.key, { value: state, enumerable: false, writable: false, configurable: true });
  return state;
};

module.exports = {
  StateStore: StateStore,
  rawTarget: rawTarget
};
