import React from "react";
import { SwapVertical } from "./SwapVertical";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SwapVertical",
  component: SwapVertical,
};

export const Default = () => <SwapVertical />;
export const Fill = () => <SwapVertical fill="blue" />;
export const Size = () => <SwapVertical height="50" width="50" />;
export const CustomStyle = () => <SwapVertical style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwapVertical className="custom-class" />;

export const Clickable = () => <SwapVertical onClick={()=>console.log("clicked")} />;

const Template = (args) => <SwapVertical {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
