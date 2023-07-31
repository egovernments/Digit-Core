import React from "react";
import { RequestQuote } from "./RequestQuote";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RequestQuote",
  component: RequestQuote,
};

export const Default = () => <RequestQuote />;
export const Fill = () => <RequestQuote fill="blue" />;
export const Size = () => <RequestQuote height="50" width="50" />;
export const CustomStyle = () => <RequestQuote style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RequestQuote className="custom-class" />;

export const Clickable = () => <RequestQuote onClick={()=>console.log("clicked")} />;

const Template = (args) => <RequestQuote {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
