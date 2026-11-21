import React from "react";
import { Call } from "./Call";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Call",
  component: Call,
};

export const Default = () => <Call />;
export const Fill = () => <Call fill="blue" />;
export const Size = () => <Call height="50" width="50" />;
export const CustomStyle = () => <Call style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Call className="custom-class" />;

export const Clickable = () => <Call onClick={()=>console.log("clicked")} />;

const Template = (args) => <Call {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
