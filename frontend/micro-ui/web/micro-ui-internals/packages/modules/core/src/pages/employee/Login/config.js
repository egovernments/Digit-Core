export const loginConfig = [
  {
    texts: {
      header: "CORE_COMMON_LOGIN",
      submitButtonLabel: "CORE_COMMON_CONTINUE",
      secondaryButtonLabel: "CORE_COMMON_FORGOT_PASSWORD",
    },
    inputs: [
      {
        label: "CORE_LOGIN_USERNAME",
        type: "text",
        populators: {
          name: "username",
        },
        isMandatory: true,
      },
      {
        label: "CORE_LOGIN_PASSWORD",
        type: "password",
        populators: {
          name: "password",
        },
        isMandatory: true,
      },
      {
        isMandatory: true,
        type: "dropdown",
        key: "city",
        label: "CORE_COMMON_CITY",
        disable: false,
        populators: {
          name: "city",
          optionsKey: "name",
          error: "ERR_HRMS_INVALID_CITY",
          mdmsConfig: {
            masterName: "tenants",
            moduleName: "tenant",
            localePrefix: "TENANT_TENANTS",
            select:
              "(data)=>{ return Array.isArray(data['tenant'].tenants) && Digit.Utils.getUnique(data['tenant'].tenants).map(ele=>({code:ele.code,name:Digit.Utils.locale.getTransformedLocale('TENANT_TENANTS_'+ele.code)}))}",
          },
        },
      },
    ],
  },
];
