/**
 * Custom util to get the config based on the JSON Schema and UI Schema.
 *
 * @author jagankumar-egov
 *
 * @example
 *   Digit.Hooks.workbench.UIFormBodyGenerator({},{})
 *
 * @returns {Array<object>} Returns the Form Body config
 */
const UIFormBodyGenerator = (JSONSchema = {}, UISchema = {}) => {
  // const newConfig=[];
  const schema = JSONSchema?.properties;
  const referenceSchema = JSONSchema?.["x-ref-schema"] || [];

  const body = Object.keys(schema).map((property) => {
    let bodyConfig = {};
    const referenceSchemaObject = referenceSchema?.filter((e) => e.fieldPath == property);
    if (referenceSchemaObject && Array.isArray(referenceSchemaObject) && referenceSchemaObject.length > 0) {
      /* TODO Schema fetch also has to be integrated */
      bodyConfig = Digit.Utils.workbench.getConfig("select");
      const masterDetails = referenceSchemaObject?.[0]?.schemaCode?.split?.(".");
      bodyConfig.populators.mdmsConfig.moduleName = masterDetails?.[0];
      bodyConfig.populators.mdmsConfig.masterName = masterDetails?.[1];
      bodyConfig.populators.mdmsConfig.localePrefix = Digit.Utils.locale.getTransformedLocale(referenceSchemaObject?.[0]?.schemaCode);
      bodyConfig.key = "SELECT" + property;
      bodyConfig.populators.name = "SELECT" + property;
    } else {
      bodyConfig = Digit.Utils.workbench.getConfig(schema[property].type);
      bodyConfig.key = schema[property].type == "boolean" ? "SELECT" + property : property;
      bodyConfig.populators.name = schema[property].type == "boolean" ? "SELECT" + property : property;
    }

    bodyConfig.label = Digit.Utils.workbench.getMDMSLabel(property);

    bodyConfig.isMandatory = JSONSchema?.required?.includes?.(property);
    return { ...bodyConfig };
  });
  return body;
};

/**
 * Custom util to get the config based on the JSON Schema (MDMS schema) and UI Schema.
 *
 * @author jagankumar-egov
 *
 * @example
 *   Digit.Hooks.workbench.UICreateConfigGenerator({},{})
 *
 * @returns {Array<object>} Returns the Create screen config
 */
const UICreateConfigGenerator = (MDMSSchema = {}, UISchema = {}) => {
  // const newConfig=[];
  const body = UIFormBodyGenerator(MDMSSchema.definition, UISchema);
  const newConfig = [{ head: Digit.Utils.workbench.getMDMSLabel(MDMSSchema?.code), subHead: MDMSSchema?.description, body }];

  return newConfig;
};

/**
 * Custom util to get the mdms context path.
 *
 * @author jagankumar-egov
 *
 * @example
 *   Digit.Hooks.workbench.getMDMSContextPath()
 *
 * @returns {Array<object>} Returns the Create screen config
 */
const getMDMSContextPath = () => {
  return window?.globalConfigs?.getConfig("MDMS_CONTEXT_PATH") || "mdms-v2";
};

export { UICreateConfigGenerator, getMDMSContextPath };
