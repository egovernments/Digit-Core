import React from "react";
import { Duo } from "./Duo";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Duo",
  component: Duo,
};

export const Default = () => <Duo />;
export const Fill = () => <Duo fill="blue" />;
export const Size = () => <Duo height="50" width="50" />;
export const CustomStyle = () => <Duo style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Duo className="custom-class" />;

export const Clickable = () => <Duo onClick={()=>console.log("clicked")} />;

const Template = (args) => <Duo {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
