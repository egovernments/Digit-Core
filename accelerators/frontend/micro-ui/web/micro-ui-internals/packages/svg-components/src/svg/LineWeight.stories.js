import React from "react";
import { LineWeight } from "./LineWeight";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LineWeight",
  component: LineWeight,
};

export const Default = () => <LineWeight />;
export const Fill = () => <LineWeight fill="blue" />;
export const Size = () => <LineWeight height="50" width="50" />;
export const CustomStyle = () => <LineWeight style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LineWeight className="custom-class" />;

export const Clickable = () => <LineWeight onClick={()=>console.log("clicked")} />;

const Template = (args) => <LineWeight {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
