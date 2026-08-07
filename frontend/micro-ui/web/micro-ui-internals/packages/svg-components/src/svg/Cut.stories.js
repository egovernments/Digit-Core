import React from "react";
import { Cut } from "./Cut";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Cut",
  component: Cut,
};

export const Default = () => <Cut />;
export const Fill = () => <Cut fill="blue" />;
export const Size = () => <Cut height="50" width="50" />;
export const CustomStyle = () => <Cut style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Cut className="custom-class" />;

export const Clickable = () => <Cut onClick={()=>console.log("clicked")} />;

const Template = (args) => <Cut {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
