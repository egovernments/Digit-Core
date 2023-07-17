/**
 * Custom util to get the config based on the JSON Schema and UI Schema.
 *
 * @author jagankumar-egov
 *
 * @example
 *   Digit.Hooks.workbench.UICreateConfigGenerator({},{})
 *
 * @returns {Array<object>} Returns the Form Body config
 */
const UIFormBodyGenerator = (JSONSchema = {}, UISchema = {}) => {
  // const newConfig=[];
  const schema = JSONSchema?.properties;
  const body = Object.keys(schema).map((property) => {
    const bodyConfig = Digit.Utils.workbench.getConfig(schema[property].type);

    bodyConfig.label = Digit.Utils.workbench.getMDMSLabel(property);
    bodyConfig.key = property;
    bodyConfig.populators.name = property;
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

export { UICreateConfigGenerator };
