import React from "react";
import { ScreenShare } from "./ScreenShare";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ScreenShare",
  component: ScreenShare,
};

export const Default = () => <ScreenShare />;
export const Fill = () => <ScreenShare fill="blue" />;
export const Size = () => <ScreenShare height="50" width="50" />;
export const CustomStyle = () => <ScreenShare style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ScreenShare className="custom-class" />;

export const Clickable = () => <ScreenShare onClick={()=>console.log("clicked")} />;

const Template = (args) => <ScreenShare {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
