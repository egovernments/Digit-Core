import React from "react";
import { LocalPizza } from "./LocalPizza";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalPizza",
  component: LocalPizza,
};

export const Default = () => <LocalPizza />;
export const Fill = () => <LocalPizza fill="blue" />;
export const Size = () => <LocalPizza height="50" width="50" />;
export const CustomStyle = () => <LocalPizza style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalPizza className="custom-class" />;

export const Clickable = () => <LocalPizza onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalPizza {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
