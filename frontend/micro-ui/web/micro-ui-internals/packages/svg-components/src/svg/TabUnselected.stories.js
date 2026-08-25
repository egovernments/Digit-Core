import React from "react";
import { TabUnselected } from "./TabUnselected";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TabUnselected",
  component: TabUnselected,
};

export const Default = () => <TabUnselected />;
export const Fill = () => <TabUnselected fill="blue" />;
export const Size = () => <TabUnselected height="50" width="50" />;
export const CustomStyle = () => <TabUnselected style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TabUnselected className="custom-class" />;
export const Clickable = () => <TabUnselected onClick={()=>console.log("clicked")} />;

const Template = (args) => <TabUnselected {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
