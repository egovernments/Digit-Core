import React from "react";
import { HourglassFull } from "./HourglassFull";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HourglassFull",
  component: HourglassFull,
};

export const Default = () => <HourglassFull />;
export const Fill = () => <HourglassFull fill="blue" />;
export const Size = () => <HourglassFull height="50" width="50" />;
export const CustomStyle = () => <HourglassFull style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HourglassFull className="custom-class" />;

export const Clickable = () => <HourglassFull onClick={()=>console.log("clicked")} />;

const Template = (args) => <HourglassFull {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
