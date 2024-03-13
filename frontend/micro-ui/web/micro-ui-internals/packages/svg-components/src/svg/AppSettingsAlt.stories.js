import React from "react";
import { AppSettingsAlt } from "./AppSettingsAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AppSettingsAlt",
  component: AppSettingsAlt,
};

export const Default = () => <AppSettingsAlt />;
export const Fill = () => <AppSettingsAlt fill="blue" />;
export const Size = () => <AppSettingsAlt height="50" width="50" />;
export const CustomStyle = () => <AppSettingsAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AppSettingsAlt className="custom-class" />;
export const Clickable = () => <AppSettingsAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <AppSettingsAlt {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
