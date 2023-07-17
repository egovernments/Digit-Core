import React from "react";
import { Payments } from "./Payments";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Payments",
  component: Payments,
};

export const Default = () => <Payments />;
export const Fill = () => <Payments fill="blue" />;
export const Size = () => <Payments height="50" width="50" />;
export const CustomStyle = () => <Payments style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Payments className="custom-class" />;

export const Clickable = () => <Payments onClick={()=>console.log("clicked")} />;

const Template = (args) => <Payments {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
