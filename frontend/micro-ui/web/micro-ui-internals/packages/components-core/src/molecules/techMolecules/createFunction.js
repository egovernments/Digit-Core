export const createFunction = (functionAsString) => {
  return Function("return " + functionAsString)();
};
