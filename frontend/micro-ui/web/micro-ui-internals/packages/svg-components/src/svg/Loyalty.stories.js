import React from "react";
import { Loyalty } from "./Loyalty";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Loyalty",
  component: Loyalty,
};

export const Default = () => <Loyalty />;
export const Fill = () => <Loyalty fill="blue" />;
export const Size = () => <Loyalty height="50" width="50" />;
export const CustomStyle = () => <Loyalty style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Loyalty className="custom-class" />;

export const Clickable = () => <Loyalty onClick={()=>console.log("clicked")} />;

const Template = (args) => <Loyalty {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
