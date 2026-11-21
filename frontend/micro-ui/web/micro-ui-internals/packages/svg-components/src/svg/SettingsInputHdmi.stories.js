import React from "react";
import { SettingsInputHdmi } from "./SettingsInputHdmi";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SettingsInputHdmi",
  component: SettingsInputHdmi,
};

export const Default = () => <SettingsInputHdmi />;
export const Fill = () => <SettingsInputHdmi fill="blue" />;
export const Size = () => <SettingsInputHdmi height="50" width="50" />;
export const CustomStyle = () => <SettingsInputHdmi style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsInputHdmi className="custom-class" />;

export const Clickable = () => <SettingsInputHdmi onClick={()=>console.log("clicked")} />;

const Template = (args) => <SettingsInputHdmi {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
