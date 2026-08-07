import React from "react";
import { Hardware } from "./Hardware";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Hardware",
  component: Hardware,
};

export const Default = () => <Hardware />;
export const Fill = () => <Hardware fill="blue" />;
export const Size = () => <Hardware height="50" width="50" />;
export const CustomStyle = () => <Hardware style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Hardware className="custom-class" />;

export const Clickable = () => <Hardware onClick={()=>console.log("clicked")} />;

const Template = (args) => <Hardware {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
