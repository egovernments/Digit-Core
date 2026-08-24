const { AsyncLocalStorage } = require("async_hooks");
const axios = require("axios");

const storage = new AsyncLocalStorage();

export const runWithContext = (context, fn) => storage.run(context, fn);

export const getContext = () => storage.getStore() || {};

export const registerAxiosCorrelationInterceptor = () => {
  axios.interceptors.request.use((config) => {
    const context = getContext();
    config.headers = config.headers || {};
    if (context.CORRELATION_ID && !config.headers["x-correlation-id"]) {
      config.headers["x-correlation-id"] = context.CORRELATION_ID;
    }
    if (context.TENANTID && !config.headers["tenantId"]) {
      config.headers["tenantId"] = context.TENANTID;
    }
    return config;
  });
};
