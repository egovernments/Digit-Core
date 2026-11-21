import React from "react";
import { ViewQuilt } from "./ViewQuilt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ViewQuilt",
  component: ViewQuilt,
};

export const Default = () => <ViewQuilt />;
export const Fill = () => <ViewQuilt fill="blue" />;
export const Size = () => <ViewQuilt height="50" width="50" />;
export const CustomStyle = () => <ViewQuilt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewQuilt className="custom-class" />;
export const Clickable = () => <ViewQuilt onClick={()=>console.log("clicked")} />;

const Template = (args) => <ViewQuilt {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
