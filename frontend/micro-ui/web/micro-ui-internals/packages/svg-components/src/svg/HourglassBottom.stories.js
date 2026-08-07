import React from "react";
import { HourglassBottom } from "./HourglassBottom";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HourglassBottom",
  component: HourglassBottom,
};

export const Default = () => <HourglassBottom />;
export const Fill = () => <HourglassBottom fill="blue" />;
export const Size = () => <HourglassBottom height="50" width="50" />;
export const CustomStyle = () => <HourglassBottom style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HourglassBottom className="custom-class" />;

export const Clickable = () => <HourglassBottom onClick={()=>console.log("clicked")} />;

const Template = (args) => <HourglassBottom {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
