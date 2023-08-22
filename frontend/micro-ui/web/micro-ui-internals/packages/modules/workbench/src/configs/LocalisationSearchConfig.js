export const Config = {
  label: "WBH_LOCALISATION_SEARCH_HEADER",
  type: "search",
  actionLabel: "WBH_ADD_LOCALISATION",
  actionRole: "EMPLOYEE_COMMON",
  actionLink: "workbench/localisation-add",
  apiDetails: {
    serviceName: "/localization/messages/v1/_search",
    requestParam: {},
    requestBody: {
      
    },
    minParametersForSearchForm: 1,
    masterName: "commonUiConfig",
    moduleName: "SearchLocalisationConfig",
    tableFormJsonPath: "requestBody.custom",
    filterFormJsonPath: "requestBody.custom",
    searchFormJsonPath: "requestParam"
  },
  sections: {
    search: {
      uiConfig: {
        headerStyle: null,
        formClassName: "", //"custom-both-clear-search",
        primaryLabel: "ES_COMMON_SEARCH",
        secondaryLabel: "ES_COMMON_CLEAR_SEARCH",
        minReqFields: 1,
        defaultValues: {
          locale: "",
          module: "",
          codes: "",
          message:""
        },
        fields: [
          {
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
                select:
                    "(data)=>{ return data['common-masters'].StateInfo?.[0]?.languages }"
                } 
            },
          },
          {
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
                select:
                    "(data)=>{ return data['common-masters'].StateInfo?.[0]?.localizationModules }"
                } 
            },
          },
          {
            label: "WBH_LOC_CODE",
            type: "text",
            isMandatory: false,
            disable: false,
            populators: { 
                name: "codes",
            }
          },
          {
            label: "WBH_LOC_MESSAGE",
            type: "text",
            isMandatory: false,
            disable: false,
            populators: { 
                name: "message",
            }
          },
        ],
      },
      label: "",
      children: {},
      show: true,
    },
    searchResult: {
      label: "",
      uiConfig: {
        columns: [
          {
            label: "WBH_LOC_HEADER_MODULENAME",
            jsonPath: "module",
          },
          {
            label: "WBH_LOC_HEADER_CODE",
            jsonPath: "code",
          },
          {
            label: "WBH_LOC_HEADER_DEFAULT",
            jsonPath: "message",
          },
          {
            label: "WBH_LOC_HEADER_MESSAGE",
            jsonPath: "defaultMessage",
          },
          {
            label: "CS_COMMON_ACTION",
            // jsonPath: "message",
            // type:"action",
            svg:"EditIcon"
          },
        ],
        enableGlobalSearch: false,
        enableColumnSort: true,
        resultsJsonPath: "messages",
        manualPagination:false
      },
      children: {},
      show: true,
    },
  },
  additionalSections: {},
  customHookName:"useLocalisationSearch"
};