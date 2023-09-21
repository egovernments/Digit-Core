import React, { useState, useEffect, useReducer, useMemo, useRef,useCallback } from "react";
import {
  Card,
  CustomDropdown,
  Button,
  AddFilled,
  ActionBar,
  SubmitBar,
  Table,
  TextInput,
  LabelFieldPair,
  CardLabel,
  Header,
  Toast,
  InfoBannerIcon,
  UploadFile,
  DeleteIconv2,
  FileUploadModal,
  BreakLine,
  InfoIconOutline,
  UploadIcon
} from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import reducer, { intialState } from "../../utils/LocAddReducer";
// import sampleFile from "../../utils/file.xlsx"
import GenerateXlsx from "../../components/GenerateXlsx";

const langDropdownConfig = {
  label: "WBH_LOC_LANG",
  type: "dropdown",
  isMandatory: false,
  disable: false,
  populators: {
    name: "locale",
    optionsKey: "label",
    optionsCustomStyle: { top: "2.3rem" },
    mdmsConfig: {
      masterName: "StateInfo",
      moduleName: "common-masters",
      // localePrefix: "WBH_LOCALE_",
      select: "(data)=>{ return data['common-masters'].StateInfo?.[0]?.languages }",
    },
    styles: { width: "50%" },
  },
};

const localeDropdownConfig = {
  label: "WBH_LOC_MODULE",
  type: "dropdown",
  isMandatory: false,
  disable: false,
  populators: {
    name: "module",
    optionsKey: "label",
    optionsCustomStyle: { top: "2.3rem" },
    mdmsConfig: {
      masterName: "StateInfo",
      moduleName: "common-masters",
      // localePrefix: "WBH_LOCALE_",
      select: "(data)=>{ return data['common-masters'].StateInfo?.[0]?.localizationModules }",
    },
    styles: { width: "50%" },
  },
};


function convertObjectOfArraysToSingleArray(objectOfObjects) {
  const arrayOfArrays = Object.values(objectOfObjects)
  return [].concat(...arrayOfArrays);
}

function splitArrayIntoDynamicSubsetsByProperty(array, propertyKey) {
  const uniquePropertyValues = [...new Set(array.map(item => item[propertyKey]))];
  const numberOfSubsets = uniquePropertyValues.length;
  const subsets = Array.from({ length: numberOfSubsets }, () => []);

  array.forEach(item => {
    const propertyValue = item[propertyKey];
    const subsetIndex = uniquePropertyValues.indexOf(propertyValue);
    subsets[subsetIndex].push(item);
  });

  return subsets;
}

function filterObjectsByKeys(objArray, keysArray) {
  return objArray.map(obj => {
    const filteredObj = {};
    keysArray.forEach(key => {
      if (obj.hasOwnProperty(key)) {
        filteredObj[key] = obj[key];
      }
    });
    return filteredObj;
  });
}

function splitArrayIntoDynamicSubsetsByPropertyAndKeys(array, propertyKey, keysToInclude) {
  const uniquePropertyValuesSet = new Set(array.map(item => item[propertyKey]));
  const uniquePropertyValues = Array.from(uniquePropertyValuesSet)
  const numberOfSubsets = uniquePropertyValues.length;
  
  const subsets = Array.from({ length: numberOfSubsets }, () => []);

  array.forEach(item => {
    const propertyValue = item[propertyKey];
    const subsetIndex = uniquePropertyValues.indexOf(propertyValue);

    const subsetItem = {};
    keysToInclude.forEach(key => {
      subsetItem[key] = item[key];
    });

    subsets[subsetIndex]?.push(subsetItem);
  });

  return subsets;
}


function hasDuplicatesByKey(arr, key) {
  const seen = new Set();

  for (const obj of arr) {
    const value = obj[key];
    if (seen.has(value)) {
      return true; // Found a duplicate
    }
    seen.add(value);
  }

  return false; // No duplicates found
}

function hasFalsyValueIgnoringZero(arrOfObjects) {
  return arrOfObjects.some(obj => {
    for (const key in obj) {
      if (obj.hasOwnProperty(key) && obj[key] !== 0 && !obj[key]) {
        return true; // Found a non-zero falsy value, exit early and return true
      }
    }
    return false; // No non-zero falsy values found in this object
  });
}

const LocalisationAdd = () => {
  const [selectedLang, setSelectedLang] = useState(null);
  const [showToast, setShowToast] = useState(false);
  const [selectedModule, setSelectedModule] = useState(null);
  const stateId = Digit.ULBService.getStateId();
  const [tableState, setTableState] = useState([]);
  const { t } = useTranslation();
  const [jsonResult, setJsonResult] = useState(null);
  const [jsonResultDefault,setJsonResultDefault] = useState(null)
  const [state, dispatch] = useReducer(reducer, intialState);
  const [isDeleted,setIsDeleted] = useState(null)
  const [showBulkUploadModal,setShowBulkUploadModal] = useState(false)
  const inputRef = useRef(null);

  useEffect(() => {
    if (selectedLang && selectedModule) {
      dispatch({
        type: "CLEAR_STATE",
      });

      dispatch({
        type: "ADD_ROW",
        state: {
          code: "",
          message: "",
          locale: selectedLang.value,
          module: selectedModule.value,
          id: 0,
        },
      });
    }
  }, [selectedLang, selectedModule]);

  
  
  const handleDeleteRow =  ({row,val,col}) => {
      dispatch({
        type: "DELETE_ROW",
        state: {
          row
        },
      });
      setIsDeleted(()=>!isDeleted)
    }

  const columns = useMemo(
    () => [
      {
        Header: t("WBH_LOC_KEYCODE"),
        accessor: "code",
        Cell: ({ value, col, row, ...rest }) => {
          return (
            <TextInput
              className={"field"}
              textInputStyle={{ width: "70%", marginLeft: "2%" }}
              onChange={(e) => {
                dispatch({
                  type: "UPDATE_ROW",
                  state: {
                    row,
                    value: e.target.value,
                    // id: row.index,
                    id: row.original.id,
                    type: "keycode",
                  },
                });
              }}
              value={state.tableState[row.index]?.code}
              defaultValue={""}
              style={{ marginBottom: "0px" }}
              // onBlur={(e) => {
              //   dispatch({type:"UPDATE_ROW_KEYCODE",state:{
              //     row,
              //     value:e.target.value,
              //     id:row.index
              //   }})

              // }}
            />
          );
        },
      },
      {
        Header: t("WBH_LOC_MODULE"),
        accessor: "module",
        Cell: ({ value, col, row }) => {
          return String(value ? value : t("ES_COMMON_NA"));
        },
      },
      // {
      //   Header: t("WBH_LOC_DEFAULT_VALUE"),
      //   accessor: "defaultMessage",
      //   Cell: ({ value, col, row, ...rest }) => {
      //     return (
      //       <TextInput
      //         className={"field"}
      //         textInputStyle={{ width: "70%", marginLeft: "2%" }}
      //         disabled={true}
      //         value={state.tableState[row.index]?.message}
      //         defaultValue={""}
      //         style={{ marginBottom: "0px" }}
      //       />
      //     );
      //   }
      // },
      // {
      //   Header: t("WBH_LOC_DEFAULT_VALUE"),
      //   accessor: "module",
      //   Cell: ({ value, col, row }) => {
      //     return String(value ? value : t("ES_COMMON_NA"));
      //   },
      // },
      {
        Header: t("WBH_LOC_LOCALE"),
        accessor: "locale",
        Cell: ({ value, col, row }) => {
          return String(value ? value : t("ES_COMMON_NA"));
        },
      },
      {
        Header: (
          <div class="tooltip" style={{ marginTop: "-10px" }}>
            <span class="textoverflow" style={{ "--max-width": `20ch` }}>
              {String(t("WBH_LOC_MESSAGE_VALUE"))}
              <InfoIconOutline styles={{ marginLeft: "0.3rem",marginBottom:"-0.2rem" }}  />
            </span>
            {/* check condtion - if length greater than 20 */}
            <span class="tooltiptext" style={{ whiteSpace: "normal", width: "15rem" }}>
              {String(t("WBH_LOC_DEFAULT_INFO_MESSAGE"))}
            </span>
          </div>
        ),
        accessor: "message",
        Cell: ({ value, col, row }) => {
          return (
            <TextInput
              className={"field"}
              textInputStyle={{ width: "70%", marginLeft: "2%" }}
              onChange={(e) => {
                dispatch({
                  type: "UPDATE_ROW",
                  state: {
                    row,
                    value: e.target.value,
                    // id: row.index,
                    id: row.original.id,
                    type: "message",
                  },
                });
              }}
              value={state.tableState[row.index]?.message}
              defaultValue={""}
              style={{ marginBottom: "0px" }}
            />
          );
        },
      },
      {
        Header: t("CS_COMMON_ACTION"),
        // accessor: "code",
        Cell: ({ value, col, row, ...rest }) => {
          return (
            <span onClick={() => handleDeleteRow({row,value,col})} className="icon-wrapper">
              <DeleteIconv2 fill={"#F47738"} />
            </span>
          );
        },
      },
    ],
    [isDeleted]
  );

  const reqCriteriaAdd = {
    url: `/localization/messages/v1/_upsert`,
    params: {},
    body: {
      tenantId: stateId,
    },
    config: {
      enabled: true,
    },
  };

  const mutation = Digit.Hooks.useCustomAPIMutationHook(reqCriteriaAdd);

  const closeToast = () => {
    setTimeout(() => {
      setShowToast(null);
    }, 5000);
  };

  const handleSubmit = () => {
    const { tableState } = state;
    //validations => if any then show respective toast and return

    //same key validation
    const hasDuplicateKeycode = hasDuplicatesByKey(tableState, "code");
    const hasEmptyMessageOrCode = hasFalsyValueIgnoringZero(tableState);
    
    if (hasDuplicateKeycode) {
      setShowToast({
        label: t("WBH_LOC_SAME_KEY_VALIDATION_ERR"),
        isError: true,
      });
      closeToast()
      return;
    }
    if (hasEmptyMessageOrCode) {
      setShowToast({
        label: t("WBH_LOC_EMPTY_KEY_VALUE_VALIDATION_ERR"),
        isError: true,
      });
      closeToast()
      return;
    }

    //here create payload and call upsert

    const payloadForDefault = tableState?.map((row) => {
      return {
        ...row,
        locale: "default",
      };
    });
    const onSuccess = (resp) => {
      setShowToast({ label: `${t("WBH_LOC_UPSERT_SUCCESS")}` });
      closeToast();
      dispatch({
        type: "CLEAR_STATE",
      });
      dispatch({
        type: "ADD_ROW",
        state: {
          code: "",
          message: "",
          locale: selectedLang.value,
          module: selectedModule.value,
          id: 0,
        },
      });
      setIsDeleted(()=>!isDeleted)
    };
    const onError = (resp) => {
      let label = `${t("WBH_LOC_UPSERT_FAIL")}: `
      resp?.response?.data?.Errors?.map((err,idx) => {
        if(idx===resp?.response?.data?.Errors?.length-1){
          label = label + t(Digit.Utils.locale.getTransformedLocale(err?.code)) + '.'
        }else{
        label = label + t(Digit.Utils.locale.getTransformedLocale(err?.code)) + ', '
        }
      })
      
      setShowToast({ label, isError: true });
      closeToast();
      // dispatch({
      //   type:"CLEAR_STATE",
      // })
      // dispatch({
      //   type:"ADD_ROW",
      //   state:{
      //     code: "",
      //     message: "",
      //     locale: selectedLang.value,
      //     module: selectedModule.value,
      //     id: 0,
      //   }
      // })
    };

    mutation.mutate(
      {
        params: {},
        body: {
          tenantId: stateId,
          messages: payloadForDefault,
        },
      },
      {
        onError: () => {},
        onSuccess: () => {},
      }
    );

    mutation.mutate(
      {
        params: {},
        body: {
          tenantId: stateId,
          messages: tableState,
        },
      },
      {
        onError,
        onSuccess,
      }
    );
  };


  const handleBulkSubmit = async () => {
    //same key validation
    let hasDuplicateKeycode = false;
    jsonResult.forEach((json) => {
      if (hasDuplicatesByKey(json, "code")) {
        hasDuplicateKeycode = true;
        return;
      }
    });

    if (hasDuplicateKeycode) {
      setShowToast({
        label: "WBH_LOC_SAME_KEY_VALIDATION_ERR",
        isError: true,
      });
      return;
    }

    const promises = [];

    for (let i = 0; i < jsonResult.length; i++) {
      promises.push(
        mutation.mutateAsync(
          {
            params: {},
            body: {
              tenantId: stateId,
              messages: jsonResult[i],
            },
          }
        )
      );

      promises.push(
        mutation.mutateAsync(
          {
            params: {},
            body: {
              tenantId: stateId,
              messages: jsonResultDefault[i],
            },
          }
        )
      );
    }

    const onSuccess = (resp) => {
      setShowToast({ label: `${t("WBH_LOC_UPSERT_SUCCESS")}` });
      setShowBulkUploadModal(false)
      closeToast();
    };
    const onError = (resp) => {
      setShowBulkUploadModal(false)
      let label = `${t("WBH_LOC_UPSERT_FAIL")}: `
      resp?.response?.data?.Errors?.map((err,idx) => {
        if(idx===resp?.response?.data?.Errors?.length-1){
          label = label + t(Digit.Utils.locale.getTransformedLocale(err?.code)) + '.'
        }else{
        label = label + t(Digit.Utils.locale.getTransformedLocale(err?.code)) + ', '
        }
      })
      setShowToast({ label, isError: true });
      closeToast();
    };

    try {
      await Promise.all(promises);
      onSuccess();
    } catch (error) {
      onError(error);
    }
  };

  const handleAddRow = () => {
    dispatch({
      type: "ADD_ROW",
      state: {
        code: "",
        message: "",
        locale: selectedLang.value,
        module: selectedModule.value,
        id: state.tableState.length,
      },
    });
  };

  const onBulkUploadModalSubmit = async (file) => {
    try {
     const result = await Digit.Utils.parsingUtils.parseXlsToJsonMultipleSheetsFile(file);
     const updatedResult = convertObjectOfArraysToSingleArray(result)
     //make result for default locale
     const updatedResultDefault = updatedResult.map(row=> {
       return {
         ...row,
         locale:"default"
       }
     })
 
     const filteredResult = [filterObjectsByKeys(updatedResult,["message","module","locale","code"])]
     const filteredResultDefault = [filterObjectsByKeys(updatedResultDefault,["message","module","locale","code"])]
 
     setJsonResult(filteredResult)
     setJsonResultDefault(filteredResultDefault)
    } catch (error) {
     setShowToast({
       label: error.message || "Invalid file type. Please upload an Excel file.",
       isError: true,
     });
 
    }
   };

  const handleBulkUpload = async (event) => {
   try {
    const result = await Digit.Utils.parsingUtils.parseXlsToJsonMultipleSheets(event);
    const updatedResult = convertObjectOfArraysToSingleArray(result)
    //make result for default locale
    const updatedResultDefault = updatedResult.map(row=> {
      return {
        ...row,
        locale:"default"
      }
    })

    
    const filteredResult = [filterObjectsByKeys(updatedResult,["message","module","locale","code"])]
    const filteredResultDefault = [filterObjectsByKeys(updatedResultDefault,["message","module","locale","code"])]
   
    //commenting this code since we can upsert multiple modules in one go(reducing number of api calls)
    // const filteredResult = splitArrayIntoDynamicSubsetsByPropertyAndKeys(updatedResult,"module",["message","module","locale","code"])

    // const filteredResultDefault = splitArrayIntoDynamicSubsetsByPropertyAndKeys(updatedResultDefault,"module",["message","module","locale","code"])

    setJsonResult(filteredResult)
    setJsonResultDefault(filteredResultDefault)
    //here the result will contain all the sheets in an object
   } catch (error) {
    setShowToast({
      label: error.message || "Invalid file type. Please upload an Excel file.",
      isError: true,
    });

   }

  //   const result = await Digit.ParsingUtils.parseXlsToJsonMultipleSheets(event);
  //  const updatedResult = convertObjectOfArraysToSingleArray(result)
  //  //make result for default locale
  //  const updatedResultDefault = updatedResult.map(row=> {
  //   return {
  //     ...row,
  //     locale:"default"
  //   }
  //  })
  //  const filteredResult = splitArrayIntoDynamicSubsetsByPropertyAndKeys(updatedResult,"module",["message","module","locale","code"])
  //  const filteredResultDefault = splitArrayIntoDynamicSubsetsByPropertyAndKeys(updatedResultDefault,"module",["message","module","locale","code"])
  //  setJsonResult(filteredResult)
  //  setJsonResultDefault(filteredResultDefault)
  //  //here the result will contain all the sheets in an object
  };

  const callInputClick = async (event) => {
    inputRef.current.click();
  };

  useEffect(() => {
    if(jsonResult?.length > 0 && jsonResultDefault?.length > 0  ){
      handleBulkSubmit()
    }
  }, [jsonResult,jsonResultDefault])
  
  const fileValidator = (errMsg) => {
    setShowToast({isError:true,label:t("WBH_BULK_UPLOAD_DOC_VALIDATION_MSG")})
    closeToast()
    setShowBulkUploadModal(false)
  }


  return (
    <React.Fragment>
      <div className="jk-header-btn-wrapper">
        <Header className="works-header-search">{t("WBH_LOC_ADD")}</Header>
        <div>
          <Button
            label={t("WBH_LOC_BULK_UPLOAD_XLS")}
            variation="secondary"
            icon={<UploadIcon styles={{ height: "2.2rem", width: "2.2rem" }} />}
            type="button"
            // onButtonClick={callInputClick}
            onButtonClick={() => setShowBulkUploadModal(true)}
            className={"header-btn"}
          />
          <input className={"hide-input-type-file"} type="file" accept="xls xlsx" onChange={handleBulkUpload} />
        </div>
      </div>
      {showBulkUploadModal && (
        <FileUploadModal
          heading={"WBH_BULK_UPLOAD_HEADER"}
          cancelLabel={"WBH_LOC_EDIT_MODAL_CANCEL"}
          submitLabel={"WBH_BULK_UPLOAD_SUBMIT"}
          onSubmit={onBulkUploadModalSubmit}
          onClose={() => setShowBulkUploadModal(false)}
          t={t}
          fileValidator={fileValidator}
          onClickDownloadSample = {callInputClick}
        />
      )}
      {<GenerateXlsx inputRef={inputRef}/>}
      {/* {
        <div>
          <h2>bobbyhadz.com</h2>

          <a href={require("../../utils/file.xlsx")} download="Example-PDF-document" target="_blank" rel="noreferrer">
            <button>Download .pdf file</button>
          </a>
        </div>
      } */}
      <Card>
        <LabelFieldPair style={{ alignItems: "flex-start" }}>
          <CardLabel style={{ marginBottom: "0.4rem" }}>{t("WBH_LOC_SELECT_LANG")}</CardLabel>
          <CustomDropdown
            t={t}
            label={langDropdownConfig?.label}
            type={langDropdownConfig?.type}
            // onBlur={props.onBlur}
            value={setSelectedLang}
            // inputRef={props.ref}
            onChange={(e) => setSelectedLang(e)}
            config={langDropdownConfig?.populators}
            disable={langDropdownConfig?.disable}
            // errorStyle={errors?.[populators.name]}
          />
        </LabelFieldPair>

        <LabelFieldPair style={{ alignItems: "flex-start" }}>
          <CardLabel>{t("WBH_LOC_SELECT_MODULE")}</CardLabel>
          <CustomDropdown
            t={t}
            label={localeDropdownConfig?.label}
            type={localeDropdownConfig?.type}
            value={setSelectedLang}
            onChange={(e) => setSelectedModule(e)}
            config={localeDropdownConfig?.populators}
            disable={localeDropdownConfig?.disable}
          />
        </LabelFieldPair>
      </Card>
      {showToast && <Toast label={showToast.label} error={showToast?.isError} isDleteBtn={true} onClose={() => setShowToast(null)}></Toast>}
      {selectedLang && selectedModule && (
        <Card>
          {/* {selectedLang && selectedModule && (
          <div style={{ display: "flex" }}>
            <Button
              label={t("ADD_NEW_ROW")}
              variation="secondary"
              onButtonClick={() => {
                handleAddRow();
              }}
              type="button"
            />
            <Button
              label={t("CLEAR_LOC_TABLE")}
              variation="secondary"
              onButtonClick={() => {
                dispatch({
                  type: "CLEAR_STATE",
                });
                // dispatch({
                //   type: "ADD_ROW",
                //   state: {
                //     code: "",
                //     message: "",
                //     locale: selectedLang.value,
                //     module: selectedModule.value,
                //     id: 0,
                //   },
                // });
              }}
              type="button"
            />
          </div>
        )} */}

          {state.tableState.length > 0 && (
            <Table
              pageSizeLimit={50}
              className={"table"}
              t={t}
              customTableWrapperClassName={"dss-table-wrapper"}
              disableSort={true}
              autoSort={false}
              data={state.tableState}
              totalRecords={state.tableState.length}
              columns={columns}
              isPaginationRequired={false}
              manualPagination={false}
              getCellProps={(cellInfo) => {
                return {
                  style: {
                    padding: "20px 18px",
                    fontSize: "16px",
                    whiteSpace: "normal",
                  },
                };
              }}
            />
          )}
          <BreakLine style={{height:"0.01rem"}} />
          {selectedLang && selectedModule && (
            <div style={{ display: "flex",justifyContent:"space-between", marginTop: "2rem" }}>
              <Button
                label={t("ADD_NEW_ROW")}
                variation="secondary"
                onButtonClick={() => {
                  handleAddRow();
                }}
                type="button"
              />
              <Button
                label={t("CLEAR_LOC_TABLE")}
                variation="secondary"
                onButtonClick={() => {
                  dispatch({
                    type: "CLEAR_STATE",
                  });
                  // dispatch({
                  //   type: "ADD_ROW",
                  //   state: {
                  //     code: "",
                  //     message: "",
                  //     locale: selectedLang.value,
                  //     module: selectedModule.value,
                  //     id: 0,
                  //   },
                  // });
                }}
                type="button"
              />
            </div>
          )}
          {state.tableState.length > 0 && (
            <ActionBar>
              <SubmitBar label={t("CORE_COMMON_SAVE")} onSubmit={handleSubmit} />
            </ActionBar>
          )}
        </Card>
      )}
    </React.Fragment>
  );
};

export default LocalisationAdd;
