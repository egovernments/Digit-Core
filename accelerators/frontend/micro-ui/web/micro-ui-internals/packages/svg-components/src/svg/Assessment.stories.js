import React from "react";
import { Assessment } from "./Assessment";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Assessment",
  component: Assessment,
};

export const Default = () => <Assessment />;
export const Fill = () => <Assessment fill="blue" />;
export const Size = () => <Assessment height="50" width="50" />;
export const CustomStyle = () => <Assessment style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Assessment className="custom-class" />;
export const Clickable = () => <Assessment onClick={()=>console.log("clicked")} />;

const Template = (args) => <Assessment {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
