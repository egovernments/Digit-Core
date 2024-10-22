import React from "react";
import { LunchDining } from "./LunchDining";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LunchDining",
  component: LunchDining,
};

export const Default = () => <LunchDining />;
export const Fill = () => <LunchDining fill="blue" />;
export const Size = () => <LunchDining height="50" width="50" />;
export const CustomStyle = () => <LunchDining style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LunchDining className="custom-class" />;

export const Clickable = () => <LunchDining onClick={()=>console.log("clicked")} />;

const Template = (args) => <LunchDining {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
