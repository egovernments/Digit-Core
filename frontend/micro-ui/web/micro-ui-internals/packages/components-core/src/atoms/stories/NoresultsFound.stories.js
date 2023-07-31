import React from "react";
import NoResultsFound from "../NoResultsFound";
import { action } from "@storybook/addon-actions";

export default {
  title: "Atoms/NoResultsFound",
  component: NoResultsFound,
  argTypes: {
    className: { control: "text" },
    style: { control: "object" },
    height: { control: "number" },
    width: { control: "number" },
    onClick: { action: "NoResultsFound clicked" },
  },
};

const Template = (args) => <NoResultsFound {...args} />;

export const Default = Template.bind({});
Default.args = {};

export const WithCustomClass = Template.bind({});
WithCustomClass.args = {
  className: "custom-class",
};

export const WithCustomStyle = Template.bind({});
WithCustomStyle.args = {
  style: { color: "red", fontSize: "20px" },
};

export const WithCustomHeightAndWidth = Template.bind({});
WithCustomHeightAndWidth.args = {
  height: 32,
  width: 32,
};
