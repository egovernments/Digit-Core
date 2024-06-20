import React from "react";
import { HourglassDisabled } from "./HourglassDisabled";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HourglassDisabled",
  component: HourglassDisabled,
};

export const Default = () => <HourglassDisabled />;
export const Fill = () => <HourglassDisabled fill="blue" />;
export const Size = () => <HourglassDisabled height="50" width="50" />;
export const CustomStyle = () => <HourglassDisabled style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HourglassDisabled className="custom-class" />;

export const Clickable = () => <HourglassDisabled onClick={()=>console.log("clicked")} />;

const Template = (args) => <HourglassDisabled {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
