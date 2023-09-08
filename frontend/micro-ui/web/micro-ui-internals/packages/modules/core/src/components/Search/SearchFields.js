import { DatePicker, SubmitBar, SearchField, Button } from "@egovernments/digit-ui-components-core";
import React from "react";
import { Controller } from "react-hook-form";

const SearchFields = ({ register, control, reset, tenantId, t, previousPage, formState, isLoading }) => {
  const isMobile = window.Digit.Utils.browser.isMobile();

  return (
    <>
      <SearchField className="pt-form-field">
        <label>{t("AUDIT_FROM_DATE_LABEL")}</label>
        <Controller render={(props) => <DatePicker date={props.value} onChange={props.onChange} />} name="fromDate" control={control} />
      </SearchField>
      <SearchField className="pt-form-field">
        <label>{t("AUDIT_TO_DATE_LABEL")}</label>
        <Controller render={(props) => <DatePicker date={props.value} onChange={props.onChange} />} name="toDate" control={control} />
      </SearchField>
      <SearchField className="pt-search-action-submit">
        <Button
          style={{ marginTop: isMobile ? "510px" : "25px", marginLeft: isMobile ? "0" : "-30px", maxWidth: isMobile ? "100%" : "240px" }}
          label={t("ES_COMMON_APPLY")}
          submit
        />
      </SearchField>
    </>
  );
};
export default SearchFields;
