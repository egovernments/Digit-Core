import React from "react";
import BreadCrumb from "../BreadCrumb";

export default {
  title: "Atoms/BreadCrumb",
  component: BreadCrumb,
  argTypes: {
    className: {
      control: "text",
    },
    style: {
      control: { type: "object" },
    },
    crumbs: {
      control: { type: "object" },
    },
  },
};

const Template = (args) => <BreadCrumb {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
  crumbs: [
    {
      content: "HOME",
      show: true,
    },
    {
      content: "PAGE 1",
      show: true,
    },
  ],
};

export const Primary = Template.bind({});
Primary.args = {
  crumbs: [
    {
      content: "HOME",
      show: true,
    },
    {
      content: "PAGE 1",
      show: true,
    },
  ],
};
