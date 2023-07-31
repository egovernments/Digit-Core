import React from "react";
import { Business } from "./Business";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Business",
  component: Business,
};

export const Default = () => <Business />;
export const Fill = () => <Business fill="blue" />;
export const Size = () => <Business height="50" width="50" />;
export const CustomStyle = () => <Business style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Business className="custom-class" />;

export const Clickable = () => <Business onClick={()=>console.log("clicked")} />;

const Template = (args) => <Business {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
