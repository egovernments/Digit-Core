import React from "react";
import { Https } from "./Https";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Https",
  component: Https,
};

export const Default = () => <Https />;
export const Fill = () => <Https fill="blue" />;
export const Size = () => <Https height="50" width="50" />;
export const CustomStyle = () => <Https style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Https className="custom-class" />;

export const Clickable = () => <Https onClick={()=>console.log("clicked")} />;

const Template = (args) => <Https {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
