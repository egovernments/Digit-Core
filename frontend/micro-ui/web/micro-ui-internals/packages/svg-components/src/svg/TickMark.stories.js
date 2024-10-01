import React from "react";
import { TickMark } from "./TickMark";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TickMark",
  component: TickMark,
};

export const Default = () => <TickMark />;
export const Fill = () => <TickMark fill="blue" />;
export const Size = () => <TickMark height="50" width="50" />;
export const CustomStyle = () => <TickMark style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TickMark className="custom-class" />;

export const Clickable = () => <TickMark onClick={()=>console.log("clicked")} />;

const Template = (args) => <TickMark {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
