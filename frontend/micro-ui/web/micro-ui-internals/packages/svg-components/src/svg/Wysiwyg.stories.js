import React from "react";
import { Wysiwyg } from "./Wysiwyg";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Wysiwyg",
  component: Wysiwyg,
};

export const Default = () => <Wysiwyg />;
export const Fill = () => <Wysiwyg fill="blue" />;
export const Size = () => <Wysiwyg height="50" width="50" />;
export const CustomStyle = () => <Wysiwyg style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Wysiwyg className="custom-class" />;
export const Clickable = () => <Wysiwyg onClick={()=>console.log("clicked")} />;

const Template = (args) => <Wysiwyg {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
