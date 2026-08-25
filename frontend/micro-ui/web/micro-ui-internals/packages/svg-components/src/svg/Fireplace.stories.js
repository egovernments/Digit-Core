import React from "react";
import { Fireplace } from "./Fireplace";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Fireplace",
  component: Fireplace,
};

export const Default = () => <Fireplace />;
export const Fill = () => <Fireplace fill="blue" />;
export const Size = () => <Fireplace height="50" width="50" />;
export const CustomStyle = () => <Fireplace style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Fireplace className="custom-class" />;

export const Clickable = () => <Fireplace onClick={()=>console.log("clicked")} />;

const Template = (args) => <Fireplace {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
