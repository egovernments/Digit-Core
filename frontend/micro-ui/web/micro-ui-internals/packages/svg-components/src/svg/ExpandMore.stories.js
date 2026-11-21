import React from "react";
import { ExpandMore } from "./ExpandMore";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ExpandMore",
  component: ExpandMore,
};

export const Default = () => <ExpandMore />;
export const Fill = () => <ExpandMore fill="blue" />;
export const Size = () => <ExpandMore height="50" width="50" />;
export const CustomStyle = () => <ExpandMore style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ExpandMore className="custom-class" />;

export const Clickable = () => <ExpandMore onClick={()=>console.log("clicked")} />;

const Template = (args) => <ExpandMore {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
