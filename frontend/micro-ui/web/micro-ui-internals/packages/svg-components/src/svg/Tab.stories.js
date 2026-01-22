import React from "react";
import { Tab } from "./Tab";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Tab",
  component: Tab,
};

export const Default = () => <Tab />;
export const Fill = () => <Tab fill="blue" />;
export const Size = () => <Tab height="50" width="50" />;
export const CustomStyle = () => <Tab style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Tab className="custom-class" />;
export const Clickable = () => <Tab onClick={()=>console.log("clicked")} />;

const Template = (args) => <Tab {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
