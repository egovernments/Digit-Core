import React from "react";
import { Nat } from "./Nat";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Nat",
  component: Nat,
};

export const Default = () => <Nat />;
export const Fill = () => <Nat fill="blue" />;
export const Size = () => <Nat height="50" width="50" />;
export const CustomStyle = () => <Nat style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Nat className="custom-class" />;

export const Clickable = () => <Nat onClick={()=>console.log("clicked")} />;

const Template = (args) => <Nat {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
