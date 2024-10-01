import React from "react";
import { Moped } from "./Moped";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Moped",
  component: Moped,
};

export const Default = () => <Moped />;
export const Fill = () => <Moped fill="blue" />;
export const Size = () => <Moped height="50" width="50" />;
export const CustomStyle = () => <Moped style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Moped className="custom-class" />;

export const Clickable = () => <Moped onClick={()=>console.log("clicked")} />;

const Template = (args) => <Moped {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
