import React from "react";
import { action } from "@storybook/addon-actions";
import RemoveableTag from "../RemoveableTag";

export default {
  title: "Atoms/RemoveableTag",
  component: RemoveableTag,
};

const Template = (args) => <RemoveableTag {...args} />;

export const Default = Template.bind({});
Default.args = {
  text: "Tag 1",
  onClick: action("Tag clicked"),
};

export const WithCustomStyles = Template.bind({});
WithCustomStyles.args = {
  text: "Tag 1",
  onClick: action("Tag clicked"),
  extraStyles: {
    tagStyles: {
      backgroundColor: "#f2f2f2",
      border: "1px solid #ccc",
      borderRadius: "4px",
      padding: "4px 8px",
      margin: "4px",
    },
    textStyles: {
      color: "#333",
      fontWeight: "bold",
    },
    closeIconStyles: {
      cursor: "pointer",
      color: "red",
    },
  },
};
