import React from "react";
import { LocalPrintshop } from "./LocalPrintshop";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalPrintshop",
  component: LocalPrintshop,
};

export const Default = () => <LocalPrintshop />;
export const Fill = () => <LocalPrintshop fill="blue" />;
export const Size = () => <LocalPrintshop height="50" width="50" />;
export const CustomStyle = () => <LocalPrintshop style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalPrintshop className="custom-class" />;

export const Clickable = () => <LocalPrintshop onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalPrintshop {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
