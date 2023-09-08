import React from "react";
import { TextRotationDown } from "./TextRotationDown";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TextRotationDown",
  component: TextRotationDown,
};

export const Default = () => <TextRotationDown />;
export const Fill = () => <TextRotationDown fill="blue" />;
export const Size = () => <TextRotationDown height="50" width="50" />;
export const CustomStyle = () => <TextRotationDown style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TextRotationDown className="custom-class" />;
export const Clickable = () => <TextRotationDown onClick={()=>console.log("clicked")} />;

const Template = (args) => <TextRotationDown {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
