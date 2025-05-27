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

const getMDMSLabel = (code = "",masterName="",moduleName="",ignore=[]) => {
  let ignoreThis = ignore?.some(url => url===code)
  if(ignoreThis) {
    return null
  }
  //enable this flag to get the localisation enabled for the mdms forms
  let flag = true;
  if (!flag) {
    return code
      .split(/(?=[A-Z])/)
      .reduce((acc, curr) => acc + curr.charAt(0).toUpperCase() + curr.slice(1) + " ", "")
      .trim();
  }
  if(masterName && moduleName){
    return Digit.Utils.locale.getTransformedLocale(`SCHEMA_${moduleName}_${masterName}`);
  }
  return Digit.Utils.locale.getTransformedLocale(code);
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
/* Method currently used to find the path to insert enum to the schema*/

const getUpdatedPath = (path = "") => {
  let tempPath = path;
  if (!tempPath?.includes(".")) {
    return tempPath;
  }
  if (tempPath?.includes(".*.")) {
    tempPath = Digit.Utils.locale.stringReplaceAll(tempPath, ".*.", "_ARRAY_OBJECT_");
  }
  if (tempPath?.includes(".*")) {
    tempPath = Digit.Utils.locale.stringReplaceAll(tempPath, ".*", "_ARRAY_");
  }
  if (tempPath?.includes(".")) {
    tempPath = Digit.Utils.locale.stringReplaceAll(tempPath, ".", "_OBJECT_");
  }
  let updatedPath = Digit.Utils.locale.stringReplaceAll(tempPath, "_ARRAY_OBJECT_", ".items.properties.");
  updatedPath = Digit.Utils.locale.stringReplaceAll(updatedPath, "_ARRAY_", ".items");
  updatedPath = Digit.Utils.locale.stringReplaceAll(updatedPath, "_OBJECT_", ".properties.");
  return updatedPath;
};
/* Method currently used to update the title for the all data with localisation code*/

const updateTitleToLocalisationCodeForObject = (definition, schemaCode) => {
  Object.keys(definition?.properties).map((key) => {
    const title = Digit.Utils.locale.getTransformedLocale(`${schemaCode}_${key}`);
    definition.properties[key] = { ...definition.properties[key], title: title };
    if (definition.properties[key]?.type == "object") {
      updateTitleToLocalisationCodeForObject(definition.properties[key], schemaCode);
    }
    if (definition.properties[key]?.type == "array" && definition.properties[key]?.items?.type == "object") {
      updateTitleToLocalisationCodeForObject(definition.properties[key]?.items, schemaCode);
    }
  });
  return definition;
};

export default { getConfig, getMDMSLabel, getFormattedData, getUpdatedPath, updateTitleToLocalisationCodeForObject };
