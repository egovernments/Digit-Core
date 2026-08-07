import React from "react";
import { ScheduleSend } from "./ScheduleSend";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ScheduleSend",
  component: ScheduleSend,
};

export const Default = () => <ScheduleSend />;
export const Fill = () => <ScheduleSend fill="blue" />;
export const Size = () => <ScheduleSend height="50" width="50" />;
export const CustomStyle = () => <ScheduleSend style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ScheduleSend className="custom-class" />;

export const Clickable = () => <ScheduleSend onClick={()=>console.log("clicked")} />;

const Template = (args) => <ScheduleSend {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
