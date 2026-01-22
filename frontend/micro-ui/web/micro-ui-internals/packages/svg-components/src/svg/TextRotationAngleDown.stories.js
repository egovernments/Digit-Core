import React from "react";
import { TextRotationAngleDown } from "./TextRotationAngleDown";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TextRotationAngleDown",
  component: TextRotationAngleDown,
};

export const Default = () => <TextRotationAngleDown />;
export const Fill = () => <TextRotationAngleDown fill="blue" />;
export const Size = () => <TextRotationAngleDown height="50" width="50" />;
export const CustomStyle = () => <TextRotationAngleDown style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TextRotationAngleDown className="custom-class" />;
export const Clickable = () => <TextRotationAngleDown onClick={()=>console.log("clicked")} />;

const Template = (args) => <TextRotationAngleDown {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};