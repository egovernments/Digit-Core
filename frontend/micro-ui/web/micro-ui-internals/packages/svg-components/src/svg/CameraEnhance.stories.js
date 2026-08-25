import React from "react";
import { CameraEnhance } from "./CameraEnhance";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CameraEnhance",
  component: CameraEnhance,
};

export const Default = () => <CameraEnhance />;
export const Fill = () => <CameraEnhance fill="blue" />;
export const Size = () => <CameraEnhance height="50" width="50" />;
export const CustomStyle = () => <CameraEnhance style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CameraEnhance className="custom-class" />;

export const Clickable = () => <CameraEnhance onClick={()=>console.log("clicked")} />;

const Template = (args) => <CameraEnhance {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
