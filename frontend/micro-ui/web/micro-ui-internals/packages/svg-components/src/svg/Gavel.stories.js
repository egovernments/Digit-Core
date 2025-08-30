import React from "react";
import { Gavel } from "./Gavel";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Gavel",
  component: Gavel,
};

export const Default = () => <Gavel />;
export const Fill = () => <Gavel fill="blue" />;
export const Size = () => <Gavel height="50" width="50" />;
export const CustomStyle = () => <Gavel style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Gavel className="custom-class" />;

export const Clickable = () => <Gavel onClick={()=>console.log("clicked")} />;

const Template = (args) => <Gavel {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
