import React from "react";
import LabelFieldPair from "../LabelFieldPair";

export default {
  title: "Atoms/LabelFieldPair",
  component: LabelFieldPair,
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

const Template = (args) => <LabelFieldPair {...args} />;

export const Default = Template.bind({});
Default.args = {
  children: (
    <>
      <label>Field Label:</label>
      <input type="text" placeholder="Enter value" />
    </>
  ),
};
