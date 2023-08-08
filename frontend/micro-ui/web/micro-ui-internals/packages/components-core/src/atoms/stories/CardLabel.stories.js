import React from "react";
import CardLabel from "../CardLabel";

export default {
  title: "Atoms/CardLabel",
  component: CardLabel,
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

const Template = (args) => <CardLabel {...args} />;

export const Default = Template.bind({});
Default.args = {
  children: "This is card label.",
};
