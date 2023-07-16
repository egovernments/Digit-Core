import React from "react";
import { SwapVerticalCircle } from "./SwapVerticalCircle";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SwapVerticalCircle",
  component: SwapVerticalCircle,
};

export const Default = () => <SwapVerticalCircle />;
export const Fill = () => <SwapVerticalCircle fill="blue" />;
export const Size = () => <SwapVerticalCircle height="50" width="50" />;
export const CustomStyle = () => <SwapVerticalCircle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwapVerticalCircle className="custom-class" />;
export const Clickable = () => <SwapVerticalCircle onClick={()=>console.log("clicked")} />;

const Template = (args) => <SwapVerticalCircle {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
