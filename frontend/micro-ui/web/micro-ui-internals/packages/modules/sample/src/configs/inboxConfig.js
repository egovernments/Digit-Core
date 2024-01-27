const inboxConfig = () => {
  return {
        "label": "HOUSEHOLD_DATA",
        "type": "search",
        "actionLabel": "HOUSEHOLD",
        "actionRole": "INDIVIDUAL_CREATOR",
        "actionLink": "masters/create-household",
        "apiDetails": {
          "serviceName": "/household/member/v1/_search?limit=20&offset=0&tenantId=mz&includeDeleted=true",
          "requestParam": {},
          "requestBody": { "apiOperation": "SEARCH", "tenantId":"mz" ,"HouseholdMember" :{"tenantId":"mz"}},
          "minParametersForSearchForm": 0,
          "masterName": "commonUiConfig",
          "moduleName": "HouseHoldConfig",
          "tableFormJsonPath": "requestParam",
        "filterFormJsonPath": "requestBody.HouseholdMember",
          "searchFormJsonPath": "requestBody.HouseholdMember"
        },
        "sections": {
          "search": {
            "uiConfig": {
              "headerStyle": null,
              "formClassName": "custom-both-clear-search",
              "primaryLabel": "ES_COMMON_SEARCH",
              "secondaryLabel": "ES_COMMON_CLEAR_SEARCH",
              "minReqFields": 0,
              "defaultValues": {
                "wardCode": "",
                "individualId": "",
                "name": "",
                "socialCategory": "",
                "mobileNumber": "",
                "createdFrom": "",
                "createdTo": ""
              },
              "fields": [
                {
                  "label": "HOUSEHOLD_ID",
                  "type": "text",
                  "isMandatory": false,
                  "disable": false,
                  "populators": {
                    "name": "householdId",
                    "error": "PROJECT_PATTERN_ERR_MSG",
                    "validation": { "minlength": 2 }
                  }
                },
                {
                  "label": "CLIENT_REF_ID",
                  "type": "text",
                  "isMandatory": false,
                  "disable": false,
                  "populators": {
                    "name": "refId",
                    "error": "PROJECT_PATTERN_ERR_MSG",
                    "validation": { "minlength": 2 }
                  }
                },
                {
                  "label": "INDIVIDUAL_ID",
                  "type": "text",
                  "isMandatory": false,
                  "disable": false,
                  "populators": {
                    "name": "individualId",
                    "error": "PROJECT_PATTERN_ERR_MSG",
                    "validation": { "minlength": 2 }
                  }
                },
                {
                  "label": "IS_HEAD",
                  "type": "text",
                  "isMandatory": false,
                  "disable": false,
                  "populators": {
                    "name": "isHead",
                    "error": "PROJECT_PATTERN_ERR_MSG",
                    "validation": { "minlength": 2 }
                  }
                }
              ]
            },
            "label": "",
            "children": {},
            "show": true
          },
          "searchResult": {
            "label": "",
            "uiConfig": {
              "columns": [
                {
                  "label": "House hold id",
                  "jsonPath": "householdId",
                },
                {
                  "label": "individual_id",
                  "jsonPath": "individualId"
                }
              ],
              "enableGlobalSearch": false,
              "enableColumnSort": true,
              "resultsJsonPath": "HouseholdMembers"
            },
            "children": {},
            "show": true
          }
        },
        "additionalSections": {}
      }
};

export default inboxConfig;
