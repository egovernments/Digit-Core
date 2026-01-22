import React from "react";
import { Grade } from "./Grade";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Grade",
  component: Grade,
};

export const Default = () => <Grade />;
export const Fill = () => <Grade fill="blue" />;
export const Size = () => <Grade height="50" width="50" />;
export const CustomStyle = () => <Grade style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Grade className="custom-class" />;

export const Clickable = () => <Grade onClick={()=>console.log("clicked")} />;

const Template = (args) => <Grade {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
