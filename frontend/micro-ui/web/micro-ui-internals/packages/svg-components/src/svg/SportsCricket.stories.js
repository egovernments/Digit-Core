import React from "react";
import { SportsCricket } from "./SportsCricket";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SportsCricket",
  component: SportsCricket,
};

export const Default = () => <SportsCricket />;
export const Fill = () => <SportsCricket fill="blue" />;
export const Size = () => <SportsCricket height="50" width="50" />;
export const CustomStyle = () => <SportsCricket style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsCricket className="custom-class" />;

export const Clickable = () => <SportsCricket onClick={()=>console.log("clicked")} />;

const Template = (args) => <SportsCricket {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
