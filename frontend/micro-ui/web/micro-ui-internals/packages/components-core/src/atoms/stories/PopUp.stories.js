import React from "react";
import PopUp from "../PopUp";

export default {
  title: "Atoms/PopUp",
  component: PopUp,
};

const Template = (args) => (
  <PopUp {...args}>
    <div style={{ padding: "20px", background: "#f2f2f2" }}>This is the content of the PopUp</div>
  </PopUp>
);

export const Default = Template.bind({});
Default.args = {};

export const WithCustomStyle = Template.bind({});
WithCustomStyle.args = {
  style: {
    background: "rgba(255, 255, 255, 0.8)",
    boxShadow: "0px 0px 10px rgba(0, 0, 0, 0.2)",
    borderRadius: "8px",
    padding: "30px",
  },
};

export const WithCustomClass = Template.bind({});
WithCustomClass.args = {
  className: "custom-popup",
};
