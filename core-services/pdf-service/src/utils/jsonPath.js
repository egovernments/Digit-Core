import { JSONPath } from "jsonpath-plus";

export const queryJsonPath = (json, path) => {
  try {
    const result = JSONPath({ path, json });
    return Array.isArray(result) ? result : [];
  } catch (e) {
    return [];
  }
};
