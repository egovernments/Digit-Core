import React from "react";
import { HourglassEmpty } from "./HourglassEmpty";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HourglassEmpty",
  component: HourglassEmpty,
};

export const Default = () => <HourglassEmpty />;
export const Fill = () => <HourglassEmpty fill="blue" />;
export const Size = () => <HourglassEmpty height="50" width="50" />;
export const CustomStyle = () => <HourglassEmpty style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HourglassEmpty className="custom-class" />;

export const Clickable = () => <HourglassEmpty onClick={()=>console.log("clicked")} />;

const Template = (args) => <HourglassEmpty {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
