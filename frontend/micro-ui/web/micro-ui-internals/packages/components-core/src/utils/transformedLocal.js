export const getTransformedLocale = (label) => {
  if (typeof label === "number") return label;
  label = label?.trim();
  return label && label.toUpperCase().replace(/[.:-\s\/]/g, "_");
};
