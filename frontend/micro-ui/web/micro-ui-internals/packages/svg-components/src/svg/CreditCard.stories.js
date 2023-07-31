import React from "react";
import { CreditCard } from "./CreditCard";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CreditCard",
  component: CreditCard,
};

export const Default = () => <CreditCard />;
export const Fill = () => <CreditCard fill="blue" />;
export const Size = () => <CreditCard height="50" width="50" />;
export const CustomStyle = () => <CreditCard style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CreditCard className="custom-class" />;

export const Clickable = () => <CreditCard onClick={()=>console.log("clicked")} />;

const Template = (args) => <CreditCard {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
