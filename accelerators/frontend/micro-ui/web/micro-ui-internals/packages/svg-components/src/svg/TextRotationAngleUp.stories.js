import React from "react";
import { TextRotationAngleUp } from "./TextRotationAngleUp";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TextRotationAngleUp",
  component: TextRotationAngleUp,
};

export const Default = () => <TextRotationAngleUp />;
export const Fill = () => <TextRotationAngleUp fill="blue" />;
export const Size = () => <TextRotationAngleUp height="50" width="50" />;
export const CustomStyle = () => <TextRotationAngleUp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TextRotationAngleUp className="custom-class" />;
export const Clickable = () => <TextRotationAngleUp onClick={()=>console.log("clicked")} />;

const Template = (args) => <TextRotationAngleUp {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
