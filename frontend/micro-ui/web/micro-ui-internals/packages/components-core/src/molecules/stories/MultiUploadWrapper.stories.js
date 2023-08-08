import React from "react";
import MultiUploadWrapper from "../MultiUploadWrapper";

export default {
  title: "Molecules/MultiUploadWrapper",
  component: MultiUploadWrapper,
};

const Template = (args) => <MultiUploadWrapper {...args} />;

// Mocking t function for translations
const t = (key) => key;

// Story for MultiUploadWrapper
export const MultiUploadStory = Template.bind({});
MultiUploadStory.args = {
  t: t,
  module: "PGR",
  tenantId: "YOUR_TENANT_ID",
  getFormState: (state) => console.log("Form state:", state),
  extraStyleName: "YOUR_EXTRA_STYLE_NAME",
  setuploadedstate: [],
  showHintBelow: true,
  hintText: "Upload files here",
  allowedFileTypesRegex: /(.*?)(jpg|jpeg|webp|aif|png|image|pdf|msword|openxmlformats-officedocument)$/i,
  allowedMaxSizeInMB: 10,
  acceptFiles: "image/*, .jpg, .jpeg, .webp, .aif, .png, .image, .pdf, .msword, .openxmlformats-officedocument, .dxf",
  maxFilesAllowed: 5,
  customClass: "YOUR_CUSTOM_CLASS",
  customErrorMsg: "YOUR_CUSTOM_ERROR_MESSAGE",
  containerStyles: { backgroundColor: "lightgray", padding: "10px" },
};
