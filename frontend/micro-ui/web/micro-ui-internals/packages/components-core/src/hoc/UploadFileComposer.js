import React from "react";
import PropTypes from "prop-types";
import { useTranslation } from "react-i18next";
import LabelFieldPair from "../atoms/LabelFieldPair";
import CardLabel from "../atoms/CardLabel";
import CardLabelError from "../atoms/CardLabelError";
import CitizenInfoLabel from "../atoms/CitizenInfoLabel";
import Header from "../atoms/Header";
import MultiUploadWrapper from "../molecules/MultiUploadWrapper";
import TextInput from "../atoms/TextInput";
import { getRegex } from "../utils/uploadFileComposerUtils";
import useCustomMDMS from "./techHoc/useCustomMDMS";

const UploadFileComposer = ({ module, config, Controller, control, register, formData, errors, localePrefix, customClass, customErrorMsg }) => {
  const { t } = useTranslation();

  //fetch mdms config based on module name
  const tenant = Digit?.ULBService?.getStateId();
  const { isLoading, data } = useCustomMDMS(tenant, "works", [
    {
      name: "DocumentConfig",
      filter: `[?(@.module=='${module}')]`,
    },
  ]);

  const docConfig = data?.works?.DocumentConfig?.[0];

  return (
    <React.Fragment>
      <Header styles={{ fontSize: "24px", marginTop: "40px" }}>{t("WORKS_RELEVANT_DOCUMENTS")}</Header>
      <CitizenInfoLabel info={t("ES_COMMON_INFO")} text={t(docConfig?.bannerLabel)} className="digit-doc-banner"></CitizenInfoLabel>
      {docConfig?.documents?.map((item, index) => {
        if (!item?.active) return;
        return (
          <LabelFieldPair key={index} style={{ alignItems: item?.showTextInput ? "flex-start" : "center" }}>
            {item.code && (
              <CardLabel className="bolder" style={{ marginTop: item?.showTextInput ? "10px" : "" }}>
                {t(`${localePrefix}_${item?.code}`)} {item?.isMandatory ? " * " : null}
              </CardLabel>
            )}

            <div className="digit-field">
              {item?.showTextInput ? (
                <TextInput
                  style={{ marginBottom: "16px" }}
                  name={`${config?.name}.${item?.name}_name`}
                  placeholder={t("ES_COMMON_ENTER_NAME")}
                  inputRef={register({ minLength: 2 })}
                />
              ) : null}
              <div style={{ marginBottom: "24px" }}>
                <Controller
                  render={({ value = [], onChange }) => {
                    function getFileStoreData(filesData) {
                      const numberOfFiles = filesData.length;
                      let finalDocumentData = [];
                      if (numberOfFiles > 0) {
                        filesData.forEach((value) => {
                          finalDocumentData.push({
                            fileName: value?.[0],
                            fileStoreId: value?.[1]?.fileStoreId?.fileStoreId,
                            documentType: value?.[1]?.file?.type,
                          });
                        });
                      }
                      onChange(numberOfFiles > 0 ? filesData : []);
                    }
                    return (
                      <MultiUploadWrapper
                        t={t}
                        module="works"
                        getFormState={getFileStoreData}
                        setuploadedstate={value || []}
                        showHintBelow={item?.hintText ? true : false}
                        hintText={item?.hintText}
                        allowedFileTypesRegex={getRegex(item?.allowedFileTypes)}
                        allowedMaxSizeInMB={item?.maxSizeInMB || docConfig?.maxSizeInMB || 5}
                        maxFilesAllowed={item?.maxFilesAllowed || 1}
                        customErrorMsg={item?.customErrorMsg}
                        customClass={customClass}
                        tenantId={Digit.ULBService.getCurrentTenantId()}
                      />
                    );
                  }}
                  rules={{
                    validate: (value) => {
                      return !(item?.isMandatory && value?.length === 0);
                    },
                  }}
                  defaultValue={formData?.[item?.name]}
                  name={`${config?.name}.${item?.name}`}
                  control={control}
                />
                {errors && errors[`${config?.name}`]?.[`${item?.name}`] && Object.keys(errors[`${config?.name}`]?.[`${item?.name}`]).length ? (
                  <CardLabelError style={{ fontSize: "12px" }}>{t(config?.error)}</CardLabelError>
                ) : null}
              </div>
            </div>
          </LabelFieldPair>
        );
      })}
    </React.Fragment>
  );
};

UploadFileComposer.propTypes = {
  module: PropTypes.string.isRequired,
  config: PropTypes.object.isRequired,
  Controller: PropTypes.func.isRequired,
  control: PropTypes.object.isRequired,
  register: PropTypes.func.isRequired,
  formData: PropTypes.object.isRequired,
  errors: PropTypes.object.isRequired,
  localePrefix: PropTypes.string.isRequired,
  customClass: PropTypes.string,
  customErrorMsg: PropTypes.string,
  data: PropTypes.object.isRequired,
  tenant: PropTypes.object.isRequired,
};

export default UploadFileComposer;
