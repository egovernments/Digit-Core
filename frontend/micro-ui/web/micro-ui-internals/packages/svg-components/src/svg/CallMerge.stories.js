import React from "react";
import { CallMerge } from "./CallMerge";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CallMerge",
  component: CallMerge,
};

export const Default = () => <CallMerge />;
export const Fill = () => <CallMerge fill="blue" />;
export const Size = () => <CallMerge height="50" width="50" />;
export const CustomStyle = () => <CallMerge style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CallMerge className="custom-class" />;

export const Clickable = () => <CallMerge onClick={()=>console.log("clicked")} />;

const Template = (args) => <CallMerge {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
