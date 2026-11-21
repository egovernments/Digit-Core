import React from "react";
import { PowerSettingsNew } from "./PowerSettingsNew";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PowerSettingsNew",
  component: PowerSettingsNew,
};

export const Default = () => <PowerSettingsNew />;
export const Fill = () => <PowerSettingsNew fill="blue" />;
export const Size = () => <PowerSettingsNew height="50" width="50" />;
export const CustomStyle = () => <PowerSettingsNew style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PowerSettingsNew className="custom-class" />;

export const Clickable = () => <PowerSettingsNew onClick={()=>console.log("clicked")} />;

const Template = (args) => <PowerSettingsNew {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
