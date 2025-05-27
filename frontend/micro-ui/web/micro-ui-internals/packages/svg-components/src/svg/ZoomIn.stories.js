import React from "react";
import { ZoomIn } from "./ZoomIn";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ZoomIn",
  component: ZoomIn,
};

export const Default = () => <ZoomIn />;
export const Fill = () => <ZoomIn fill="blue" />;
export const Size = () => <ZoomIn height="50" width="50" />;
export const CustomStyle = () => <ZoomIn style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ZoomIn className="custom-class" />;
export const Clickable = () => <ZoomIn onClick={()=>console.log("clicked")} />;

const Template = (args) => <ZoomIn {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
