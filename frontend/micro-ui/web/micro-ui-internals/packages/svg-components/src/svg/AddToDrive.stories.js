import React from "react";
import { AddToDrive } from "./AddToDrive";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AddToDrive",
  component: AddToDrive,
};

export const Default = () => <AddToDrive />;
export const Fill = () => <AddToDrive fill="blue" />;
export const Size = () => <AddToDrive height="50" width="50" />;
export const CustomStyle = () => <AddToDrive style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddToDrive className="custom-class" />;
export const Clickable = () => <AddToDrive onClick={()=>console.log("clicked")} />;

const Template = (args) => <AddToDrive {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
