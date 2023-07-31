import React from "react";
import { Plumbing } from "./Plumbing";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Plumbing",
  component: Plumbing,
};

export const Default = () => <Plumbing />;
export const Fill = () => <Plumbing fill="blue" />;
export const Size = () => <Plumbing height="50" width="50" />;
export const CustomStyle = () => <Plumbing style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Plumbing className="custom-class" />;

export const Clickable = () => <Plumbing onClick={()=>console.log("clicked")} />;

const Template = (args) => <Plumbing {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
