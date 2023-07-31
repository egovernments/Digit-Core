import React from "react";
import { HelpOutline } from "./HelpOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HelpOutline",
  component: HelpOutline,
};

export const Default = () => <HelpOutline />;
export const Fill = () => <HelpOutline fill="blue" />;
export const Size = () => <HelpOutline height="50" width="50" />;
export const CustomStyle = () => <HelpOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HelpOutline className="custom-class" />;

export const Clickable = () => <HelpOutline onClick={()=>console.log("clicked")} />;

const Template = (args) => <HelpOutline {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
