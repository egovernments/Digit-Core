import React from "react";
import { PlusOne } from "./PlusOne";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PlusOne",
  component: PlusOne,
};

export const Default = () => <PlusOne />;
export const Fill = () => <PlusOne fill="blue" />;
export const Size = () => <PlusOne height="50" width="50" />;
export const CustomStyle = () => <PlusOne style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PlusOne className="custom-class" />;

export const Clickable = () => <PlusOne onClick={()=>console.log("clicked")} />;

const Template = (args) => <PlusOne {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
