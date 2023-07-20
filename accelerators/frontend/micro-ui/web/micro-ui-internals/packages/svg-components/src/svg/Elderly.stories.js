import React from "react";
import { Elderly } from "./Elderly";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Elderly",
  component: Elderly,
};

export const Default = () => <Elderly />;
export const Fill = () => <Elderly fill="blue" />;
export const Size = () => <Elderly height="50" width="50" />;
export const CustomStyle = () => <Elderly style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Elderly className="custom-class" />;

export const Clickable = () => <Elderly onClick={()=>console.log("clicked")} />;

const Template = (args) => <Elderly {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
