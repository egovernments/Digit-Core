import React from "react";
import { SettingsPhone } from "./SettingsPhone";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SettingsPhone",
  component: SettingsPhone,
};

export const Default = () => <SettingsPhone />;
export const Fill = () => <SettingsPhone fill="blue" />;
export const Size = () => <SettingsPhone height="50" width="50" />;
export const CustomStyle = () => <SettingsPhone style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsPhone className="custom-class" />;

export const Clickable = () => <SettingsPhone onClick={()=>console.log("clicked")} />;

const Template = (args) => <SettingsPhone {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
