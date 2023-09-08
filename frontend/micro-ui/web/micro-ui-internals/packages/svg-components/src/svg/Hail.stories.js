import React from "react";
import { Hail } from "./Hail";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Hail",
  component: Hail,
};

export const Default = () => <Hail />;
export const Fill = () => <Hail fill="blue" />;
export const Size = () => <Hail height="50" width="50" />;
export const CustomStyle = () => <Hail style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Hail className="custom-class" />;

export const Clickable = () => <Hail onClick={()=>console.log("clicked")} />;

const Template = (args) => <Hail {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
