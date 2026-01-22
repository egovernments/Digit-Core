import React from "react";
import { RuleFolder } from "./RuleFolder";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RuleFolder",
  component: RuleFolder,
};

export const Default = () => <RuleFolder />;
export const Fill = () => <RuleFolder fill="blue" />;
export const Size = () => <RuleFolder height="50" width="50" />;
export const CustomStyle = () => <RuleFolder style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RuleFolder className="custom-class" />;

export const Clickable = () => <RuleFolder onClick={()=>console.log("clicked")} />;

const Template = (args) => <RuleFolder {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
