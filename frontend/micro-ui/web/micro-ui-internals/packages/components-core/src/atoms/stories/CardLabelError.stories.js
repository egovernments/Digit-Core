import React from "react";
import CardLabelError from "../CardLabelError";

export default {
  title: "Atoms/CardLabelError",
  component: CardLabelError,
  argTypes: {
    children: {
      control: { type: "text" },
    },
    className: {
      control: { type: "text" },
    },
    style: {
      control: { type: "object" },
    },
  },
};

const Template = (args) => <CardLabelError {...args} />;

export const Default = Template.bind({});
Default.args = {
  children: "This is an error label.",
};
