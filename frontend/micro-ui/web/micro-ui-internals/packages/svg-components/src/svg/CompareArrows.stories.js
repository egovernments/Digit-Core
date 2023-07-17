import React from "react";
import { CompareArrows } from "./CompareArrows";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CompareArrows",
  component: CompareArrows,
};

export const Default = () => <CompareArrows />;
export const Fill = () => <CompareArrows fill="blue" />;
export const Size = () => <CompareArrows height="50" width="50" />;
export const CustomStyle = () => <CompareArrows style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CompareArrows className="custom-class" />;

export const Clickable = () => <CompareArrows onClick={()=>console.log("clicked")} />;

const Template = (args) => <CompareArrows {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
