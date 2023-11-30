import React from "react";
import { BakeryDining } from "./BakeryDining";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "BakeryDining",
  component: BakeryDining,
};

export const Default = () => <BakeryDining />;
export const Fill = () => <BakeryDining fill="blue" />;
export const Size = () => <BakeryDining height="50" width="50" />;
export const CustomStyle = () => <BakeryDining style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BakeryDining className="custom-class" />;

export const Clickable = () => <BakeryDining onClick={()=>console.log("clicked")} />;

const Template = (args) => <BakeryDining {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
