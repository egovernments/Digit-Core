import React from "react";
import { Dangerous } from "./Dangerous";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Dangerous",
  component: Dangerous,
};

export const Default = () => <Dangerous />;
export const Fill = () => <Dangerous fill="blue" />;
export const Size = () => <Dangerous height="50" width="50" />;
export const CustomStyle = () => <Dangerous style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Dangerous className="custom-class" />;

export const Clickable = () => <Dangerous onClick={()=>console.log("clicked")} />;

const Template = (args) => <Dangerous {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
