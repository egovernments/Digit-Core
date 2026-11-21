import React from "react";
import { Fastfood } from "./Fastfood";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Fastfood",
  component: Fastfood,
};

export const Default = () => <Fastfood />;
export const Fill = () => <Fastfood fill="blue" />;
export const Size = () => <Fastfood height="50" width="50" />;
export const CustomStyle = () => <Fastfood style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Fastfood className="custom-class" />;

export const Clickable = () => <Fastfood onClick={()=>console.log("clicked")} />;

const Template = (args) => <Fastfood {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
