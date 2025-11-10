import React from "react";
import { SettingsEthernet } from "./SettingsEthernet";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SettingsEthernet",
  component: SettingsEthernet,
};

export const Default = () => <SettingsEthernet />;
export const Fill = () => <SettingsEthernet fill="blue" />;
export const Size = () => <SettingsEthernet height="50" width="50" />;
export const CustomStyle = () => <SettingsEthernet style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsEthernet className="custom-class" />;

export const Clickable = () => <SettingsEthernet onClick={()=>console.log("clicked")} />;

const Template = (args) => <SettingsEthernet {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
