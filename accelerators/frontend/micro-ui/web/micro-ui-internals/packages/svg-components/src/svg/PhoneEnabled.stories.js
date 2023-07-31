import React from "react";
import { PhoneEnabled } from "./PhoneEnabled";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PhoneEnabled",
  component: PhoneEnabled,
};

export const Default = () => <PhoneEnabled />;
export const Fill = () => <PhoneEnabled fill="blue" />;
export const Size = () => <PhoneEnabled height="50" width="50" />;
export const CustomStyle = () => <PhoneEnabled style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PhoneEnabled className="custom-class" />;

export const Clickable = () => <PhoneEnabled onClick={()=>console.log("clicked")} />;

const Template = (args) => <PhoneEnabled {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
