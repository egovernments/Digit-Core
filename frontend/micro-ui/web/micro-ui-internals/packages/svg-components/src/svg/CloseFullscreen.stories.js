import React from "react";
import { CloseFullscreen } from "./CloseFullscreen";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CloseFullscreen",
  component: CloseFullscreen,
};

export const Default = () => <CloseFullscreen />;
export const Fill = () => <CloseFullscreen fill="blue" />;
export const Size = () => <CloseFullscreen height="50" width="50" />;
export const CustomStyle = () => <CloseFullscreen style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CloseFullscreen className="custom-class" />;

export const Clickable = () => <CloseFullscreen onClick={()=>console.log("clicked")} />;

const Template = (args) => <CloseFullscreen {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
