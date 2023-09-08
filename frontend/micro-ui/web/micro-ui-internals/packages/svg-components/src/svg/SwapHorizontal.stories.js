import React from "react";
import { SwapHorizontal } from "./SwapHorizontal";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SwapHorizontal",
  component: SwapHorizontal,
};

export const Default = () => <SwapHorizontal />;
export const Fill = () => <SwapHorizontal fill="blue" />;
export const Size = () => <SwapHorizontal height="50" width="50" />;
export const CustomStyle = () => <SwapHorizontal style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwapHorizontal className="custom-class" />;
export const Clickable = () => <SwapHorizontal onClick={()=>console.log("clicked")} />;

const Template = (args) => <SwapHorizontal {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
