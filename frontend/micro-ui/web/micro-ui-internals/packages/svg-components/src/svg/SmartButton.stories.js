import React from "react";
import { SmartButton } from "./SmartButton";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SmartButton",
  component: SmartButton,
};

export const Default = () => <SmartButton />;
export const Fill = () => <SmartButton fill="blue" />;
export const Size = () => <SmartButton height="50" width="50" />;
export const CustomStyle = () => <SmartButton style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SmartButton className="custom-class" />;

export const Clickable = () => <SmartButton onClick={()=>console.log("clicked")} />;

const Template = (args) => <SmartButton {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
