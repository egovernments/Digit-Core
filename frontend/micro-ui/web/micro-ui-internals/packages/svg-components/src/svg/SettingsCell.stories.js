import React from "react";
import { SettingsCell } from "./SettingsCell";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SettingsCell",
  component: SettingsCell,
};

export const Default = () => <SettingsCell />;
export const Fill = () => <SettingsCell fill="blue" />;
export const Size = () => <SettingsCell height="50" width="50" />;
export const CustomStyle = () => <SettingsCell style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsCell className="custom-class" />;

export const Clickable = () => <SettingsCell onClick={()=>console.log("clicked")} />;

const Template = (args) => <SettingsCell {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
