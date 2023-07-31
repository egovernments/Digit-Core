import React from "react";
import { LocalBar } from "./LocalBar";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalBar",
  component: LocalBar,
};

export const Default = () => <LocalBar />;
export const Fill = () => <LocalBar fill="blue" />;
export const Size = () => <LocalBar height="50" width="50" />;
export const CustomStyle = () => <LocalBar style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalBar className="custom-class" />;

export const Clickable = () => <LocalBar onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalBar {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
