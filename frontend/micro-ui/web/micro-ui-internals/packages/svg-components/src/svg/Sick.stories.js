import React from "react";
import { Sick } from "./Sick";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Sick",
  component: Sick,
};

export const Default = () => <Sick />;
export const Fill = () => <Sick fill="blue" />;
export const Size = () => <Sick height="50" width="50" />;
export const CustomStyle = () => <Sick style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Sick className="custom-class" />;

export const Clickable = () => <Sick onClick={()=>console.log("clicked")} />;

const Template = (args) => <Sick {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
