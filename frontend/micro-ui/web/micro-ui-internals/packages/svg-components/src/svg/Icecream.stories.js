import React from "react";
import { Icecream } from "./Icecream";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Icecream",
  component: Icecream,
};

export const Default = () => <Icecream />;
export const Fill = () => <Icecream fill="blue" />;
export const Size = () => <Icecream height="50" width="50" />;
export const CustomStyle = () => <Icecream style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Icecream className="custom-class" />;

export const Clickable = () => <Icecream onClick={()=>console.log("clicked")} />;

const Template = (args) => <Icecream {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
