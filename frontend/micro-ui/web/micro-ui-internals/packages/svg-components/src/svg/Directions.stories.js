import React from "react";
import { Directions } from "./Directions";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Directions",
  component: Directions,
};

export const Default = () => <Directions />;
export const Fill = () => <Directions fill="blue" />;
export const Size = () => <Directions height="50" width="50" />;
export const CustomStyle = () => <Directions style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Directions className="custom-class" />;

export const Clickable = () => <Directions onClick={()=>console.log("clicked")} />;

const Template = (args) => <Directions {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
