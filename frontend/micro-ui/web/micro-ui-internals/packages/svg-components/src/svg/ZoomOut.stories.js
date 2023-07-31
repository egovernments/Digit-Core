import React from "react";
import { ZoomOut } from "./ZoomOut";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ZoomOut",
  component: ZoomOut,
};

export const Default = () => <ZoomOut />;
export const Fill = () => <ZoomOut fill="blue" />;
export const Size = () => <ZoomOut height="50" width="50" />;
export const CustomStyle = () => <ZoomOut style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ZoomOut className="custom-class" />;
export const Clickable = () => <ZoomOut onClick={()=>console.log("clicked")} />;

const Template = (args) => <ZoomOut {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
