import React from "react";
import { PermCameraMic } from "./PermCameraMic";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PermCameraMic",
  component: PermCameraMic,
};

export const Default = () => <PermCameraMic />;
export const Fill = () => <PermCameraMic fill="blue" />;
export const Size = () => <PermCameraMic height="50" width="50" />;
export const CustomStyle = () => <PermCameraMic style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PermCameraMic className="custom-class" />;

export const Clickable = () => <PermCameraMic onClick={()=>console.log("clicked")} />;

const Template = (args) => <PermCameraMic {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
