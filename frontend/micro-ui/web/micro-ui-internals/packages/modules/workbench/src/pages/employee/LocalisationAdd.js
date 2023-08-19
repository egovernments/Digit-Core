import React, { useState, useEffect, useReducer, useMemo } from "react";
import {
  Card,
  CustomDropdown,
  Button,
  ActionBar,
  SubmitBar,
  Table,
  TextInput,
  LabelFieldPair,
  CardLabel,
  Header,
  Toast,
} from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import reducer, { intialState } from "../../utils/LocAddReducer";

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

const LocalisationAdd = () => {
  const [selectedLang, setSelectedLang] = useState(null);
  const [showToast, setShowToast] = useState(false);
  const [selectedModule, setSelectedModule] = useState(null);
  const stateId = Digit.ULBService.getStateId();
  const [tableState, setTableState] = useState([]);
  const { t } = useTranslation();

  const [state, dispatch] = useReducer(reducer, intialState);

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
                    id: row.index,
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
        Header: t("WBH_LOC_MESSAGE_VALUE"),
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
                    id: row.index,
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
    ],
    []
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
    const hasDuplicateKeycode = hasDuplicatesByKey(tableState,"code")

    if(hasDuplicateKeycode){
      setShowToast({
        label:"WBH_LOC_SAME_KEY_VALIDATION_ERR",
        isError:true
      })
      return 
    }
    
    //here create payload and call upsert
    
    const payloadForDefault = tableState?.map(row => {
      return {
        ...row,
        locale:"default"
      }
    })
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
    };
    const onError = (resp) => {
      setShowToast({ label: `${t("WBH_LOC_UPSERT_FAIL")}`, isError: true });
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
        onError:()=>{},
        onSuccess:()=>{},
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

  return (
    <React.Fragment>
      <Header className="works-header-search">{t("WBH_LOC_ADD")}</Header>
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

        {state.tableState.length > 0 && (
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
              }}
              type="button"
            />
          </div>
        )}

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
            styles={{ marginTop: "3rem" }}
          />
        )}

        {state.tableState.length > 0 && (
          <ActionBar>
            <SubmitBar label={t("CORE_COMMON_SAVE")} onSubmit={handleSubmit} />
          </ActionBar>
        )}
        {showToast && <Toast label={t(showToast.label)} error={showToast?.isError}></Toast>}
      </Card>
    </React.Fragment>
  );
};

export default LocalisationAdd;
