import React from "react";
import { Hotel } from "./Hotel";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Hotel",
  component: Hotel,
};

export const Default = () => <Hotel />;
export const Fill = () => <Hotel fill="blue" />;
export const Size = () => <Hotel height="50" width="50" />;
export const CustomStyle = () => <Hotel style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Hotel className="custom-class" />;

export const Clickable = () => <Hotel onClick={()=>console.log("clicked")} />;

const Template = (args) => <Hotel {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
