export const Config = {
  label: "WBH_SEARCH_MDMS",
  type: "search",
  actionLabel: "WBH_ADD_MDMS",
  actionRole: "SUPERUSER",
  actionLink: "workbench/mdms-add",
  apiDetails: {
    serviceName: "/egov-mdms-service/v1/_search",
    requestParam: {},
    requestBody: {
      MdmsCriteria: {
        moduleDetails: [
         {
          moduleName: "ACCESSCONTROL-ROLES",
          masterDetails: [
            {
              name: "roles",
              // filter: "",
              // "filter": "[?(@.code=='HRMS_ADMIN')]"
            },
          ],
         }
        ],
      },
    },
    minParametersForSearchForm: 1,
    masterName: "commonUiConfig",
    moduleName: "SearchMDMSConfig",
    tableFormJsonPath: "requestBody.MdmsCriteria.moduleDetails[0].masterDetails[0].custom",
    filterFormJsonPath: "requestBody.MdmsCriteria.moduleDetails[0].masterDetails[0].custom",
    searchFormJsonPath: "requestBody.MdmsCriteria.moduleDetails[0].masterDetails[0].custom"
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
          value: "",
          field: "",
          createdFrom: "",
          createdTo: "",
        },
        fields: [
          {
            label: "WBH_FIELD",
            type: "dropdown",
            isMandatory: false,
            disable: false,
            populators: {
              name: "field",
              optionsKey: "code",
              optionsCustomStyle: { top: "2.3rem" },
              options: [
                {
                  code: "code",
                  name: "code",
                },
                {
                  code: "name",
                  name: "name",
                },
                {
                  code: "description",
                  name: "description",
                },
              ],
            },
          },
          {
            label: "WBH_FIELD_VALUE",
            type: "text",
            isMandatory: false,
            disable: false,
            populators: {
              name: "value",
              validation: { pattern: {}, maxlength: 140 },
            },
          },

          {
            label: "CREATED_FROM_DATE",
            type: "date",
            isMandatory: false,
            disable: false,
            key: "createdFrom",
            preProcess: {
              updateDependent: ["populators.max"],
            },
            populators: { name: "createdFrom", max: "currentDate" },
          },
          {
            label: "CREATED_TO_DATE",
            type: "date",
            isMandatory: false,
            disable: false,
            key: "createdTo",
            preProcess: {
              updateDependent: ["populators.max"],
            },
            populators: {
              name: "createdTo",
              error: "DATE_VALIDATION_MSG",
              max: "currentDate",
            },
            additionalValidation: {
              type: "date",
              keys: { start: "createdFrom", end: "createdTo" },
            },
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
            label: "MASTERS_WAGESEEKER_ID",
            jsonPath: "businessObject.individualId",
            additionalCustomization: true,
          },
          {
            label: "MASTERS_WAGESEEKER_NAME",
            jsonPath: "businessObject.name.givenName",
          },
          { label: "MASTERS_FATHER_NAME", jsonPath: "businessObject.fatherName" },
          {
            label: "MASTERS_SOCIAL_CATEGORY",
            jsonPath: "businessObject.additionalFields.fields[0].value",
          },
          {
            label: "CORE_COMMON_PROFILE_CITY",
            jsonPath: "businessObject.address[0].tenantId",
            additionalCustomization: true,
          },
          {
            label: "MASTERS_WARD",
            jsonPath: "businessObject.address[0].ward.code",
            additionalCustomization: true,
          },
          {
            label: "MASTERS_LOCALITY",
            jsonPath: "businessObject.address[0].locality.code",
            additionalCustomization: true,
          },
        ],
        enableGlobalSearch: false,
        enableColumnSort: true,
        resultsJsonPath: "items",
      },
      children: {},
      show: true,
    },
  },
  additionalSections: {},
};
