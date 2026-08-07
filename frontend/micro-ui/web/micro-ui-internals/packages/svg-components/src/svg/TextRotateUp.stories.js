import React from "react";
import { TextRotateUp } from "./TextRotateUp";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TextRotateUp",
  component: TextRotateUp,
};

export const Default = () => <TextRotateUp />;
export const Fill = () => <TextRotateUp fill="blue" />;
export const Size = () => <TextRotateUp height="50" width="50" />;
export const CustomStyle = () => <TextRotateUp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TextRotateUp className="custom-class" />;
export const Clickable = () => <TextRotateUp onClick={()=>console.log("clicked")} />;

const Template = (args) => <TextRotateUp {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
