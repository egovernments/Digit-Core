import React from "react";
import { Rtt } from "./Rtt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Rtt",
  component: Rtt,
};

export const Default = () => <Rtt />;
export const Fill = () => <Rtt fill="blue" />;
export const Size = () => <Rtt height="50" width="50" />;
export const CustomStyle = () => <Rtt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Rtt className="custom-class" />;

export const Clickable = () => <Rtt onClick={()=>console.log("clicked")} />;

const Template = (args) => <Rtt {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
