import React from "react";
import { SouthWest } from "./SouthWest";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SouthWest",
  component: SouthWest,
};

export const Default = () => <SouthWest />;
export const Fill = () => <SouthWest fill="blue" />;
export const Size = () => <SouthWest height="50" width="50" />;
export const CustomStyle = () => <SouthWest style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SouthWest className="custom-class" />;

export const Clickable = () => <SouthWest onClick={()=>console.log("clicked")} />;

const Template = (args) => <SouthWest {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
