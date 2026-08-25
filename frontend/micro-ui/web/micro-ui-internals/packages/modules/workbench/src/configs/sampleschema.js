export const schema = {
  $schema: "http://json-schema.org/draft-07/schema#",
  title: "Generated schema for Root",
  type: "object",
  properties: {
    name: {
      type: "string",
    },
    code: {
      type: "string",
    },
    qrCodeURL: {
      type: "string",
    },
    bannerUrl: {
      type: "string",
    },
    logoUrl: {
      type: "string",
    },
    logoUrlWhite: {
      type: "string",
    },
    statelogo: {
      type: "string",
    },
    gender: {
      type: "string",
    },
    hasLocalisation: {
      type: "boolean",
    },
    enableWhatsApp: {
      type: "boolean",
    },
    mobileNo: {
      type: "number",
    },
    defaultUrl: {
      type: "object",
      properties: {
        citizen: {
          type: "string",
        },
        employee: {
          type: "string",
        },
      },
      required: ["citizen", "employee"],
    },
    languages: {
      type: "array",
      items: {
        type: "object",
        properties: {
          label: {
            type: "string",
          },
          value: {
            type: "string",
          },
        },
        required: ["label", "value"],
      },
    },
    sampleObject:{
      type: "array",
      items: {
          type: "string",        
      },
      required: ["label", "value"],
    },
    localizationModules: {
      type: "array",
      items: {
        type: "object",
        properties: {
          label: {
            type: "string",
          },
          value: {
            type: "string",
          },
        },
        required: ["label", "value"],
      },
    },
  },
  required: [
    "name",
    "code",
    "logoUrl",
    "statelogo",
    "hasLocalisation",
    "enableWhatsApp",
    "mobileNo",
    "defaultUrl",
    "languages",
    "localizationModules",
  ],
};

export const mdmsSchema = {
  tenantId: "pg",
  code: "common-masters.StateInfo",
  description: "StateInfo",
  definition: {
    ...schema,
    "x-unique": ["code"],
    "x-ref-schema": [
      {
        fieldPath: "gender",
        schemaCode: "common-masters.GenderType",
      },
    ],
  },
  isActive: true,
};
