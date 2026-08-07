import React from "react";
import { LocalConvenienceStore } from "./LocalConvenienceStore";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalConvenienceStore",
  component: LocalConvenienceStore,
};

export const Default = () => <LocalConvenienceStore />;
export const Fill = () => <LocalConvenienceStore fill="blue" />;
export const Size = () => <LocalConvenienceStore height="50" width="50" />;
export const CustomStyle = () => <LocalConvenienceStore style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalConvenienceStore className="custom-class" />;

export const Clickable = () => <LocalConvenienceStore onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalConvenienceStore {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
