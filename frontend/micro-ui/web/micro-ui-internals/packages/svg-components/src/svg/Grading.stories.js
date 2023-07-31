import React from "react";
import { Grading } from "./Grading";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Grading",
  component: Grading,
};

export const Default = () => <Grading />;
export const Fill = () => <Grading fill="blue" />;
export const Size = () => <Grading height="50" width="50" />;
export const CustomStyle = () => <Grading style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Grading className="custom-class" />;

export const Clickable = () => <Grading onClick={()=>console.log("clicked")} />;

const Template = (args) => <Grading {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
