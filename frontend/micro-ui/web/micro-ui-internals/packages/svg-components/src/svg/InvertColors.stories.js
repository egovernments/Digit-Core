import React from "react";
import { InvertColors } from "./InvertColors";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "InvertColors",
  component: InvertColors,
};

export const Default = () => <InvertColors />;
export const Fill = () => <InvertColors fill="blue" />;
export const Size = () => <InvertColors height="50" width="50" />;
export const CustomStyle = () => <InvertColors style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <InvertColors className="custom-class" />;

export const Clickable = () => <InvertColors onClick={()=>console.log("clicked")} />;

const Template = (args) => <InvertColors {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
