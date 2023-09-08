import React from "react";
import { Clock } from "./Clock";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Clock",
  component: Clock,
};

export const Default = () => <Clock />;
export const Fill = () => <Clock fill="blue" />;
export const Size = () => <Clock height="50" width="50" />;
export const CustomStyle = () => <Clock style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Clock className="custom-class" />;

export const Clickable = () => <Clock onClick={()=>console.log("clicked")} />;

const Template = (args) => <Clock {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
