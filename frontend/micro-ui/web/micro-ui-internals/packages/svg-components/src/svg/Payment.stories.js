import React from "react";
import { Payment } from "./Payment";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Payment",
  component: Payment,
};

export const Default = () => <Payment />;
export const Fill = () => <Payment fill="blue" />;
export const Size = () => <Payment height="50" width="50" />;
export const CustomStyle = () => <Payment style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Payment className="custom-class" />;

export const Clickable = () => <Payment onClick={()=>console.log("clicked")} />;

const Template = (args) => <Payment {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
