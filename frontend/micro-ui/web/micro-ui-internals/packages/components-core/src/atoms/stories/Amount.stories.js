import React from "react";
import Amount from "../Amount";

export default {
  title: "Atom/Amount",
  component: Amount,
};

const Template = (args) => <Amount {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { color: "green" },
  roundOff: true,
  value: 550010
};

