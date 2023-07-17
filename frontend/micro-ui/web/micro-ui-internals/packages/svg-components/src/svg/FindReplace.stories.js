import React from "react";
import { FindReplace } from "./FindReplace";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FindReplace",
  component: FindReplace,
};

export const Default = () => <FindReplace />;
export const Fill = () => <FindReplace fill="blue" />;
export const Size = () => <FindReplace height="50" width="50" />;
export const CustomStyle = () => <FindReplace style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FindReplace className="custom-class" />;

export const Clickable = () => <FindReplace onClick={()=>console.log("clicked")} />;

const Template = (args) => <FindReplace {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
