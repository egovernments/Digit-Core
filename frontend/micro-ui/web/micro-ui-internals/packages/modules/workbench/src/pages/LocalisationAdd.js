import React, { useState,useEffect,useReducer,useMemo } from "react";
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
  Header
} from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import reducer,{intialState} from "../utils/LocAddReducer"

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

const LocalisationAdd = () => {
  const [selectedLang, setSelectedLang] = useState(null);
  const [selectedModule, setSelectedModule] = useState(null);
  
  const [tableState, setTableState] = useState([]);
  const { t } = useTranslation();

  const [state,dispatch] = useReducer(reducer,intialState)
console.log("outside",state.tableState);
  useEffect(() => {
    if(selectedLang && selectedModule){

      dispatch({
        type:"CLEAR_STATE",
      })

      dispatch({
        type:"ADD_ROW",
        state:[{
          code: "",
          message: "",
          locale: selectedLang.value,
          module: selectedModule.value,
          id: 0,
        }]
      })

      // setTableState([{
      //   code: "code",
      //   message: "message",
      //   locale: selectedLang.value,
      //   module: selectedModule.value,
      //   id: 0,
      // }])
    }

    if(!selectedLang || !selectedModule){
      // setTableState([])
      // dispatch({
      //   type:"CLEAR_STATE",
      // })
      // dispatch({
      //   type:"ADD_ROW",
      //   state:[{
      //     code: "",
      //     message: "",
      //     locale: selectedLang.value,
      //     module: selectedModule.value,
      //     id: 0,
      //   }]
      // })
    }
  }, [selectedLang,selectedModule])
  

  const columns = useMemo(() => ([
    {
      Header: t("WBH_LOC_KEYCODE"),
      accessor: "code",
      Cell: ({ value, col, row, ...rest }) => {
        return (
          <TextInput
            className={"field"}
            textInputStyle={{ width: "70%", marginLeft: "2%" }}
            onChange={(e) => {
              dispatch({type:"UPDATE_ROW_KEYCODE",state:{
                row,
                value:e.target.value,
                id:row.index
              }})
              
            }}
            // value={state.tableState[row.index][0].code}
            defaultValue={""}
            style={{marginBottom:"0px"}}
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
      Header: t("WBH_LOC_DEFAULT_VALUE"),
      accessor: "module",
      Cell: ({ value, col, row }) => {
        return String(row.original?.[0].module ? row.original?.[0].module : t("ES_COMMON_NA"));
      },
    },
    {
      Header: t("WBH_LOC_LOCALE"),
      accessor: "locale",
      Cell: ({ value, col, row }) => {
        return String(row.original?.[0].locale ? row.original?.[0].locale : t("ES_COMMON_NA"));
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
              console.log(e.target.value);
            }}
            value={state.tableState[row.index]?.[0].message}
            defaultValue={""}
            style={{marginBottom:"0px"}}
          />
        ); 
      },
    },
  ]), [])

  // const columns =  [
  //   {
  //     Header: t("WBH_LOC_KEYCODE"),
  //     accessor: "code",
  //     Cell: ({ value, col, row, ...rest }) => {
  //       return (
  //         <TextInput
  //           className={"field"}
  //           textInputStyle={{ width: "70%", marginLeft: "2%" }}
  //           onChange={(e) => {
  //             dispatch({type:"UPDATE_ROW_KEYCODE",state:{
  //               row,
  //               value:e.target.value,
  //               id:row.index
  //             }})
              
  //           }}
  //           // value={state.tableState[row.index][0].code}
  //           defaultValue={""}
  //           style={{marginBottom:"0px"}}
  //           // onBlur={(e) => {
  //           //   dispatch({type:"UPDATE_ROW_KEYCODE",state:{
  //           //     row,
  //           //     value:e.target.value,
  //           //     id:row.index
  //           //   }})
              
  //           // }}
  //         />
  //       );
  //     },
  //   },
  //   {
  //     Header: t("WBH_LOC_DEFAULT_VALUE"),
  //     accessor: "module",
  //     Cell: ({ value, col, row }) => {
  //       return String(row.original[0].module ? row.original[0].module : t("ES_COMMON_NA"));
  //     },
  //   },
  //   {
  //     Header: t("WBH_LOC_LOCALE"),
  //     accessor: "locale",
  //     Cell: ({ value, col, row }) => {
  //       return String(row.original[0].locale ? row.original[0].locale : t("ES_COMMON_NA"));
  //     },
  //   },
  //   {
  //     Header: t("WBH_LOC_MESSAGE_VALUE"),
  //     accessor: "message",
  //     Cell: ({ value, col, row }) => {
  //       return (
  //         <TextInput
  //           className={"field"}
  //           textInputStyle={{ width: "70%", marginLeft: "2%" }}
  //           onChange={(e) => {
  //             console.log(e.target.value);
  //           }}
  //           value={state.tableState[row.index][0].message}
  //           defaultValue={""}
  //           style={{marginBottom:"0px"}}
  //         />
  //       ); 
  //     },
  //   },
  // ];

  const handleSubmit = () => {};
  const handleAddRow = () => {
    dispatch({
      type:"ADD_ROW",
      state:[{
        code: "",
        message: "",
        locale: selectedLang.value,
        module: selectedModule.value,
        id: state.tableState.length,
      }]
    })
  }

  return (
    <React.Fragment>
      <Header className="works-header-search">{t("WBH_LOC_ADD")}</Header>
    <Card>
      <LabelFieldPair style={{alignItems:"flex-start"}}>
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

      <LabelFieldPair style={{alignItems:"flex-start"}}>
        <CardLabel>{t("WBH_LOC_SELECT_MODULE")}</CardLabel>
        <CustomDropdown
          t={t}
          label={localeDropdownConfig?.label}
          type={localeDropdownConfig?.type}
          // onBlur={props.onBlur}
          value={setSelectedLang}
          // inputRef={props.ref}
          onChange={(e) => setSelectedModule(e)}
          config={localeDropdownConfig?.populators}
          disable={localeDropdownConfig?.disable}
          // errorStyle={errors?.[populators.name]}
        />
      </LabelFieldPair>

      {state.tableState.length>0 && <Button
        label={t("ADD_NEW_ROW")}
        variation="primary"
        onButtonClick={() => {
          handleAddRow()
        }}
        type="button"
      />}

      {state.tableState.length>0 && <Table
        className={"table"}
        t={t}
        customTableWrapperClassName={"dss-table-wrapper"}
        disableSort={true}
        autoSort={false}
        data={state.tableState}
        totalRecords={state.tableState.length}
        columns={columns}
        isPaginationRequired={true}
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
      />}

      <ActionBar>
        <SubmitBar label={t("CORE_COMMON_SAVE")} onSubmit={handleSubmit} />
      </ActionBar>
    </Card>
    </React.Fragment>
  );
};

export default LocalisationAdd;
