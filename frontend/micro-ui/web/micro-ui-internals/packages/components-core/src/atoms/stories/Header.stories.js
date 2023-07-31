import React from "react";
import Header from "../Header";

export default {
  title: "Atoms/Header",
  component: Header,
  argTypes: {
    className: {
      control: "text",
    },
    styles: {
      control: "object",
    },
    children: {
      control: "object",
    },
  },
};

const Template = (args) => <Header {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  styles: {
    border: "1px solid green",
  },
  children: <p>This is a Custom Header</p>,
};

export const Primary = Template.bind({});
Primary.args = {
  children: <p>This is a Primary Header</p>,
};