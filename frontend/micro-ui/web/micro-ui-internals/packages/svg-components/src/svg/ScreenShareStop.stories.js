import React from "react";
import { ScreenShareStop } from "./ScreenShareStop";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ScreenShareStop",
  component: ScreenShareStop,
};

export const Default = () => <ScreenShareStop />;
export const Fill = () => <ScreenShareStop fill="blue" />;
export const Size = () => <ScreenShareStop height="50" width="50" />;
export const CustomStyle = () => <ScreenShareStop style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ScreenShareStop className="custom-class" />;

export const Clickable = () => <ScreenShareStop onClick={()=>console.log("clicked")} />;

const Template = (args) => <ScreenShareStop {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
