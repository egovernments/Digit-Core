import React from "react";
import { Extension } from "./Extension";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Extension",
  component: Extension,
};

export const Default = () => <Extension />;
export const Fill = () => <Extension fill="blue" />;
export const Size = () => <Extension height="50" width="50" />;
export const CustomStyle = () => <Extension style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Extension className="custom-class" />;

export const Clickable = () => <Extension onClick={()=>console.log("clicked")} />;

const Template = (args) => <Extension {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
