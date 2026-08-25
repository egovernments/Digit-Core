import React from "react";
import { NoTransfer } from "./NoTransfer";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NoTransfer",
  component: NoTransfer,
};

export const Default = () => <NoTransfer />;
export const Fill = () => <NoTransfer fill="blue" />;
export const Size = () => <NoTransfer height="50" width="50" />;
export const CustomStyle = () => <NoTransfer style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NoTransfer className="custom-class" />;

export const Clickable = () => <NoTransfer onClick={()=>console.log("clicked")} />;

const Template = (args) => <NoTransfer {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
