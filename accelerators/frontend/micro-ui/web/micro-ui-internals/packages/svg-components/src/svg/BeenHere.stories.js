import React from "react";
import { BeenHere } from "./BeenHere";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "BeenHere",
  component: BeenHere,
};

export const Default = () => <BeenHere />;
export const Fill = () => <BeenHere fill="blue" />;
export const Size = () => <BeenHere height="50" width="50" />;
export const CustomStyle = () => <BeenHere style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BeenHere className="custom-class" />;

export const Clickable = () => <BeenHere onClick={()=>console.log("clicked")} />;

const Template = (args) => <BeenHere {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
