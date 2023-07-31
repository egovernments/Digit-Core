const CONFIGS_TEMPLATE = {
  string: {
    inline: true,
    label: "",
    isMandatory: false,
    type: "text",
    disable: false,
    populators: {
      name: "text1",
      error: "invalid text field",
      validation: {
        pattern: /^[A-Za-z]+$/i,
      },
    },
  },
  object: {
    inline: true,
    label: "",
    isMandatory: false,
    type: "object",
    disable: false,
    populators: {
      name: "text1",
      error: "invalid text field",
      validation: {
        pattern: /^[A-Za-z]+$/i,
      },
    },
  }, 
  array: {
    inline: true,
    label: "",
    isMandatory: false,
    type: "array",
    disable: false,
    populators: {
      name: "text1",
      error: "invalid text field",
      validation: {
        pattern: /^[A-Za-z]+$/i,
      },
    },
  },
  number: {
    label: "",
    isMandatory: false,
    type: "number",
    disable: false,
    populators: {
      name: "number1",
      error: "invalid number field",
      validation: {
        min: 0,
        max: 9999999999,
      },
    },
  },
  boolean: {
    isMandatory: false,
    key: "radio1",
    type: "radio",
    label: "",
    disable: false,
    populators: {
      name: "radio1",
      optionsKey: "name",
      error: "select any one value",
      required: false,
      options: [
        {
          code: true,
          name: "True",
        },
        {
          code: false,
          name: "false",
        },
      ],
    },
  },
  select: {
    isMandatory: false,
    type: "dropdown",
    label: "",
    disable: false,
    populators: {
      name: "select1",
      optionsKey: "name",
      error: "select any one value",
      required: false,
      mdmsConfig: {
        masterName: "",
        moduleName: "",
        localePrefix: "",
      },
    },
  },
  default: {
    inline: true,
    label: "",
    isMandatory: false,
    type: "text",
    disable: false,
    populators: {
      name: "text1",
      error: "invalid text field",
      validation: {
        pattern: /^[A-Za-z]+$/i,
      },
    },
  },
};

const getConfig = (type = "text") => {
  const config = CONFIGS_TEMPLATE?.[type] ? CONFIGS_TEMPLATE?.[type] : CONFIGS_TEMPLATE?.default;
  return {
    ...config,
    populators: {
      ...config.populators,
    },
  };
};

const getMDMSLabel = (code = "") => {
  //enable this flag to get the localisation enabled for the mdms forms
  let flag;
  if (!flag) {
    return code
      .split(/(?=[A-Z])/)
      .reduce((acc, curr) => acc + curr.charAt(0).toUpperCase() + curr.slice(1) + " ", "")
      .trim();
  }
  return Digit.Utils.locale.getTransformedLocale(
    code
      .split(/(?=[A-Z])/)
      .reduce((acc, curr) => acc + curr.charAt(0).toUpperCase() + curr.slice(1) + " ", "")
      .trim()
  );
};

const getFormattedData = (data = {}) => {
  const formattedData = {};
  Object.keys(data).map((key) => {
    if (key?.startsWith("SELECT")) {
      const newKey = key?.replace("SELECT", "");
      formattedData[newKey] = data[newKey]?.code;
    } else {
      formattedData[key] = data[key];
    }
  });
  return formattedData;
};

export default { getConfig, getMDMSLabel, getFormattedData };
