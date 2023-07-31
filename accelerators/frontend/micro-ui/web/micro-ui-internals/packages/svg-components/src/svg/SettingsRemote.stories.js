import React from "react";
import { SettingsRemote } from "./SettingsRemote";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SettingsRemote",
  component: SettingsRemote,
};

export const Default = () => <SettingsRemote />;
export const Fill = () => <SettingsRemote fill="blue" />;
export const Size = () => <SettingsRemote height="50" width="50" />;
export const CustomStyle = () => <SettingsRemote style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsRemote className="custom-class" />;

export const Clickable = () => <SettingsRemote onClick={()=>console.log("clicked")} />;

const Template = (args) => <SettingsRemote {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
