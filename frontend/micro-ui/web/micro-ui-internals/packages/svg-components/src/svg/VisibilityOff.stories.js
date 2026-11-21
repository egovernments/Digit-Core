import React from "react";
import { VisibilityOff } from "./VisibilityOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "VisibilityOff",
  component: VisibilityOff,
};

export const Default = () => <VisibilityOff />;
export const Fill = () => <VisibilityOff fill="blue" />;
export const Size = () => <VisibilityOff height="50" width="50" />;
export const CustomStyle = () => <VisibilityOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <VisibilityOff className="custom-class" />;
export const Clickable = () => <VisibilityOff onClick={()=>console.log("clicked")} />;

const Template = (args) => <VisibilityOff {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

