import React from "react";
import { SwitchLeft } from "./SwitchLeft";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SwitchLeft",
  component: SwitchLeft,
};

export const Default = () => <SwitchLeft />;
export const Fill = () => <SwitchLeft fill="blue" />;
export const Size = () => <SwitchLeft height="50" width="50" />;
export const CustomStyle = () => <SwitchLeft style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwitchLeft className="custom-class" />;
export const Clickable = () => <SwitchLeft onClick={()=>console.log("clicked")} />;

const Template = (args) => <SwitchLeft {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
