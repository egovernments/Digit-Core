import React from "react";
import { MobileScreenShare } from "./MobileScreenShare";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MobileScreenShare",
  component: MobileScreenShare,
};

export const Default = () => <MobileScreenShare />;
export const Fill = () => <MobileScreenShare fill="blue" />;
export const Size = () => <MobileScreenShare height="50" width="50" />;
export const CustomStyle = () => <MobileScreenShare style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MobileScreenShare className="custom-class" />;

export const Clickable = () => <MobileScreenShare onClick={()=>console.log("clicked")} />;

const Template = (args) => <MobileScreenShare {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
