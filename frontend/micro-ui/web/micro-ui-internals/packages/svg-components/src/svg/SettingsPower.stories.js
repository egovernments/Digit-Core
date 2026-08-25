import React from "react";
import { SettingsPower } from "./SettingsPower";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SettingsPower",
  component: SettingsPower,
};

export const Default = () => <SettingsPower />;
export const Fill = () => <SettingsPower fill="blue" />;
export const Size = () => <SettingsPower height="50" width="50" />;
export const CustomStyle = () => <SettingsPower style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsPower className="custom-class" />;

export const Clickable = () => <SettingsPower onClick={()=>console.log("clicked")} />;

const Template = (args) => <SettingsPower {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
