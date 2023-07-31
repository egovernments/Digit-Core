const amountFormatter = (value, denomination, t) => {
  const currencyFormatter = new Intl.NumberFormat("en-IN", { currency: "INR" });

  switch (denomination) {
    case "Lac":
      return `₹ ${currencyFormatter.format((value / 100000).toFixed(2) || 0)} ${t("ES_DSS_LAC")}`;
    case "Cr":
      return `₹ ${currencyFormatter.format((value / 10000000).toFixed(2) || 0)} ${t("ES_DSS_CR")}`;
    case "Unit":
      return `₹ ${currencyFormatter.format(value?.toFixed(2) || 0)}`;
    default:
      return "";
  }
};

export const formatter = (value, symbol, unit, commaSeparated = true, t) => {
  if (!value && value !== 0) return "";
  switch (symbol) {
    case "amount":
      return amountFormatter(value, unit, t);
    //this case needs to be removed as backend should handle case sensitiviy from their end
    case "Amount":
      return amountFormatter(value, unit, t);
    case "number":
      if (!commaSeparated) {
        return parseInt(value);
      }
      const Nformatter = new Intl.NumberFormat("en-IN");
      return Nformatter.format(value);
    case "percentage":
      const Pformatter = new Intl.NumberFormat("en-IN", { maximumSignificantDigits: 3 });
      return `${Pformatter.format(value.toFixed(2))} %`;
    default:
      return "";
  }
};
