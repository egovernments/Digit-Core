import React from "react";
import { LineStyle } from "./LineStyle";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LineStyle",
  component: LineStyle,
};

export const Default = () => <LineStyle />;
export const Fill = () => <LineStyle fill="blue" />;
export const Size = () => <LineStyle height="50" width="50" />;
export const CustomStyle = () => <LineStyle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LineStyle className="custom-class" />;

export const Clickable = () => <LineStyle onClick={()=>console.log("clicked")} />;

const Template = (args) => <LineStyle {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
