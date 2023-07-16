import React from "react";
import { AddAlert } from "./AddAlert";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AddAlert",
  component: AddAlert,
};

export const Default = () => <AddAlert />;
export const Fill = () => <AddAlert fill="blue" />;
export const Size = () => <AddAlert height="50" width="50" />;
export const CustomStyle = () => <AddAlert style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddAlert className="custom-class" />;
export const Clickable = () => <AddAlert onClick={()=>console.log("clicked")} />;

const Template = (args) => <AddAlert {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
