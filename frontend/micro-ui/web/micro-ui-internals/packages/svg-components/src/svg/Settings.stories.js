import React from "react";
import { Settings } from "./Settings";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Settings",
  component: Settings,
};

export const Default = () => <Settings />;
export const Fill = () => <Settings fill="blue" />;
export const Size = () => <Settings height="50" width="50" />;
export const CustomStyle = () => <Settings style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Settings className="custom-class" />;

export const Clickable = () => <Settings onClick={()=>console.log("clicked")} />;

const Template = (args) => <Settings {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
