import React from "react";
import { LocalDining } from "./LocalDining";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalDining",
  component: LocalDining,
};

export const Default = () => <LocalDining />;
export const Fill = () => <LocalDining fill="blue" />;
export const Size = () => <LocalDining height="50" width="50" />;
export const CustomStyle = () => <LocalDining style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalDining className="custom-class" />;

export const Clickable = () => <LocalDining onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalDining {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
