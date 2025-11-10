import React from "react";
import { CancelScheduleSend } from "./CancelScheduleSend";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CancelScheduleSend",
  component: CancelScheduleSend,
};

export const Default = () => <CancelScheduleSend />;
export const Fill = () => <CancelScheduleSend fill="blue" />;
export const Size = () => <CancelScheduleSend height="50" width="50" />;
export const CustomStyle = () => <CancelScheduleSend style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CancelScheduleSend className="custom-class" />;

export const Clickable = () => <CancelScheduleSend onClick={()=>console.log("clicked")} />;

const Template = (args) => <CancelScheduleSend {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
