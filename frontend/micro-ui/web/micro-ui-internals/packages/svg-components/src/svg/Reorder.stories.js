import React from "react";
import { Reorder } from "./Reorder";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Reorder",
  component: Reorder,
};

export const Default = () => <Reorder />;
export const Fill = () => <Reorder fill="blue" />;
export const Size = () => <Reorder height="50" width="50" />;
export const CustomStyle = () => <Reorder style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Reorder className="custom-class" />;

export const Clickable = () => <Reorder onClick={()=>console.log("clicked")} />;

const Template = (args) => <Reorder {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
