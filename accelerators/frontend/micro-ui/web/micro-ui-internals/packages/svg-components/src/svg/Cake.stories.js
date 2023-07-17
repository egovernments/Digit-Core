import React from "react";
import { Cake } from "./Cake";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Cake",
  component: Cake,
};

export const Default = () => <Cake />;
export const Fill = () => <Cake fill="blue" />;
export const Size = () => <Cake height="50" width="50" />;
export const CustomStyle = () => <Cake style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Cake className="custom-class" />;

export const Clickable = () => <Cake onClick={()=>console.log("clicked")} />;

const Template = (args) => <Cake {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
