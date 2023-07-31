import React from "react";
import { PhonelinkLock } from "./PhonelinkLock";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PhonelinkLock",
  component: PhonelinkLock,
};

export const Default = () => <PhonelinkLock />;
export const Fill = () => <PhonelinkLock fill="blue" />;
export const Size = () => <PhonelinkLock height="50" width="50" />;
export const CustomStyle = () => <PhonelinkLock style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PhonelinkLock className="custom-class" />;

export const Clickable = () => <PhonelinkLock onClick={()=>console.log("clicked")} />;

const Template = (args) => <PhonelinkLock {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
