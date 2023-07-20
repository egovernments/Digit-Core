import React from "react";
import { Rule } from "./Rule";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Rule",
  component: Rule,
};

export const Default = () => <Rule />;
export const Fill = () => <Rule fill="blue" />;
export const Size = () => <Rule height="50" width="50" />;
export const CustomStyle = () => <Rule style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Rule className="custom-class" />;

export const Clickable = () => <Rule onClick={()=>console.log("clicked")} />;

const Template = (args) => <Rule {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
