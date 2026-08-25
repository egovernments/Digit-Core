import React from "react";
import { SettingsInputComponent } from "./SettingsInputComponent";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SettingsInputComponent",
  component: SettingsInputComponent,
};

export const Default = () => <SettingsInputComponent />;
export const Fill = () => <SettingsInputComponent fill="blue" />;
export const Size = () => <SettingsInputComponent height="50" width="50" />;
export const CustomStyle = () => <SettingsInputComponent style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsInputComponent className="custom-class" />;

export const Clickable = () => <SettingsInputComponent onClick={()=>console.log("clicked")} />;

const Template = (args) => <SettingsInputComponent {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
