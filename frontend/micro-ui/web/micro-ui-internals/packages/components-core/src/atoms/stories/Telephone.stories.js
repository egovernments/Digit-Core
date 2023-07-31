import React from "react";
import TelePhone from "../Telephone";

export default {
  title: "Atoms/TelePhone",
  component: TelePhone,
};

const Template = (args) => <TelePhone {...args} />;

export const Default = Template.bind({});
Default.args = {
  text: "Contact us at",
  mobile: "1234567890",
};

export const WithCustomStyles = Template.bind({});
WithCustomStyles.args = {
  text: "Customer Support",
  mobile: "9876543210",
  className: "custom-telephone",
  style: {
    backgroundColor: "#f2f2f2",
    padding: "10px",
    borderRadius: "5px",
  },
};
