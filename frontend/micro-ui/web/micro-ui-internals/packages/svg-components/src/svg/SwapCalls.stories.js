import React from "react";
import { SwapCalls } from "./SwapCalls";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SwapCalls",
  component: SwapCalls,
};

export const Default = () => <SwapCalls />;
export const Fill = () => <SwapCalls fill="blue" />;
export const Size = () => <SwapCalls height="50" width="50" />;
export const CustomStyle = () => <SwapCalls style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwapCalls className="custom-class" />;
export const Clickable = () => <SwapCalls onClick={()=>console.log("clicked")} />;

const Template = (args) => <SwapCalls {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

