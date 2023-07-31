import React from "react";
import { action } from "@storybook/addon-actions";
import UploadFile from "../UploadFile";

export default {
  title: "Atoms/UploadFile",
  component: UploadFile,
  argTypes: {
    message: {
      control: "text",
    },
    uploadedFiles: {
      control: {
        type: "object",
      },
    },
    showHint: {
      control: "boolean",
    },
    hintText: {
      control: "text",
    },
    iserror: {
      control: "text",
    },
    customClass: {
      control: "text",
    },
    disabled: {
      control: "boolean",
    },
    enableButton: {
      control: "boolean",
    },
    buttonType: {
      control: {
        type: "select",
        options: ["button", "submit", "reset"],
      },
    },
    inputStyles: {
      control: "object",
    },
    extraStyles: {
      control: "object",
    },
    showHintBelow: {
      control: "boolean",
    },
    id: {
      control: "text",
    },
    multiple: {
      control: "boolean",
    },
    accept: {
      control: "text",
    },
  },
};

const onUpload = (event) => {
  action("Upload")(event.target.files);
};

const removeTargetedFile = (fileDetailsData, event) => {
  event.stopPropagation();
  action("Remove File")(fileDetailsData);
};

const Template = (args) => <UploadFile onUpload={onUpload} removeTargetedFile={removeTargetedFile} {...args} />;

export const Default = Template.bind({});
Default.args = {
  message: "No files uploaded yet",
  uploadedFiles: [],
};

export const WithUploadedFiles = Template.bind({});
WithUploadedFiles.args = {
  message: "No files uploaded yet",
  uploadedFiles: [
    ["file1.jpg", { id: 1, url: "http://example.com/file1.jpg" }],
    ["file2.pdf", { id: 2, url: "http://example.com/file2.pdf" }],
  ],
};

export const WithCustomStyles = Template.bind({});
WithCustomStyles.args = {
  message: "No files uploaded yet",
  uploadedFiles: [
    ["file1.jpg", { id: 1, url: "http://example.com/file1.jpg" }],
    ["file2.pdf", { id: 2, url: "http://example.com/file2.pdf" }],
  ],
  extraStyles: {},
};

export const WithCustomHint = Template.bind({});
WithCustomHint.args = {
  message: "No files uploaded yet",
  uploadedFiles: [],
  showHint: true,
  hintText: "Upload files here",
};

export const WithError = Template.bind({});
WithError.args = {
  message: "No files uploaded yet",
  uploadedFiles: [],
  iserror: "File size exceeded the limit",
};
