import React from "react";
import { Refresh } from "./Refresh";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Refresh",
  component: Refresh,
};

export const Default = () => <Refresh />;
export const Fill = () => <Refresh fill="blue" />;
export const Size = () => <Refresh height="50" width="50" />;
export const CustomStyle = () => <Refresh style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Refresh className="custom-class" />;

export const Clickable = () => <Refresh onClick={()=>console.log("clicked")} />;

const Template = (args) => <Refresh {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
