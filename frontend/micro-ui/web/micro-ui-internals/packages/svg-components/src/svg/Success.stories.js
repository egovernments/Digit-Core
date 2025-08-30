import React from "react";
import { Success } from "./Success";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Success",
  component: Success,
};

export const Default = () => <Success />;
export const Fill = () => <Success fill="blue" />;
export const Size = () => <Success height="50" width="50" />;
export const CustomStyle = () => <Success style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Success className="custom-class" />;

export const Clickable = () => <Success onClick={()=>console.log("clicked")} />;

const Template = (args) => <Success {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
