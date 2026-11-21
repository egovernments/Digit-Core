import React from "react";
import { SwapHorizontalCircle } from "./SwapHorizontalCircle";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SwapHorizontalCircle",
  component: SwapHorizontalCircle,
};

export const Default = () => <SwapHorizontalCircle />;
export const Fill = () => <SwapHorizontalCircle fill="blue" />;
export const Size = () => <SwapHorizontalCircle height="50" width="50" />;
export const CustomStyle = () => <SwapHorizontalCircle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwapHorizontalCircle className="custom-class" />;
export const Clickable = () => <SwapHorizontalCircle onClick={()=>console.log("clicked")} />;

const Template = (args) => <SwapHorizontalCircle {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
