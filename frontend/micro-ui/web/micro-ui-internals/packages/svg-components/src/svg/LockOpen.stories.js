import React from "react";
import { LockOpen } from "./LockOpen";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LockOpen",
  component: LockOpen,
};

export const Default = () => <LockOpen />;
export const Fill = () => <LockOpen fill="blue" />;
export const Size = () => <LockOpen height="50" width="50" />;
export const CustomStyle = () => <LockOpen style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LockOpen className="custom-class" />;

export const Clickable = () => <LockOpen onClick={()=>console.log("clicked")} />;

const Template = (args) => <LockOpen {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
