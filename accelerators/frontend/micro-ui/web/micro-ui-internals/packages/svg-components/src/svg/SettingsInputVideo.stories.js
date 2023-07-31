import React from "react";
import { SettingsInputVideo } from "./SettingsInputVideo";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SettingsInputVideo",
  component: SettingsInputVideo,
};

export const Default = () => <SettingsInputVideo />;
export const Fill = () => <SettingsInputVideo fill="blue" />;
export const Size = () => <SettingsInputVideo height="50" width="50" />;
export const CustomStyle = () => <SettingsInputVideo style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsInputVideo className="custom-class" />;

export const Clickable = () => <SettingsInputVideo onClick={()=>console.log("clicked")} />;

const Template = (args) => <SettingsInputVideo {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
