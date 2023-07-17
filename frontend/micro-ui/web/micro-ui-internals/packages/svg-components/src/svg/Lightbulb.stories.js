import React from "react";
import { Lightbulb } from "./Lightbulb";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Lightbulb",
  component: Lightbulb,
};

export const Default = () => <Lightbulb />;
export const Fill = () => <Lightbulb fill="blue" />;
export const Size = () => <Lightbulb height="50" width="50" />;
export const CustomStyle = () => <Lightbulb style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Lightbulb className="custom-class" />;

export const Clickable = () => <Lightbulb onClick={()=>console.log("clicked")} />;

const Template = (args) => <Lightbulb {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
