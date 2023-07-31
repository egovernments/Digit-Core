import React from "react";
import { Sanitizer } from "./Sanitizer";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Sanitizer",
  component: Sanitizer,
};

export const Default = () => <Sanitizer />;
export const Fill = () => <Sanitizer fill="blue" />;
export const Size = () => <Sanitizer height="50" width="50" />;
export const CustomStyle = () => <Sanitizer style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Sanitizer className="custom-class" />;

export const Clickable = () => <Sanitizer onClick={()=>console.log("clicked")} />;

const Template = (args) => <Sanitizer {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
