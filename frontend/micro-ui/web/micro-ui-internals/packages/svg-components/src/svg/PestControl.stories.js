import React from "react";
import { PestControl } from "./PestControl";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PestControl",
  component: PestControl,
};

export const Default = () => <PestControl />;
export const Fill = () => <PestControl fill="blue" />;
export const Size = () => <PestControl height="50" width="50" />;
export const CustomStyle = () => <PestControl style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PestControl className="custom-class" />;

export const Clickable = () => <PestControl onClick={()=>console.log("clicked")} />;

const Template = (args) => <PestControl {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
