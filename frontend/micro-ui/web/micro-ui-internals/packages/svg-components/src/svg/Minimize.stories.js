import React from "react";
import { Minimize } from "./Minimize";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Minimize",
  component: Minimize,
};

export const Default = () => <Minimize />;
export const Fill = () => <Minimize fill="blue" />;
export const Size = () => <Minimize height="50" width="50" />;
export const CustomStyle = () => <Minimize style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Minimize className="custom-class" />;

export const Clickable = () => <Minimize onClick={()=>console.log("clicked")} />;

const Template = (args) => <Minimize {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
