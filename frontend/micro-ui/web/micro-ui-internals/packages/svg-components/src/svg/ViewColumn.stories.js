import React from "react";
import { ViewColumn } from "./ViewColumn";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ViewColumn",
  component: ViewColumn,
};

export const Default = () => <ViewColumn />;
export const Fill = () => <ViewColumn fill="blue" />;
export const Size = () => <ViewColumn height="50" width="50" />;
export const CustomStyle = () => <ViewColumn style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewColumn className="custom-class" />;
export const Clickable = () => <ViewColumn onClick={()=>console.log("clicked")} />;

const Template = (args) => <ViewColumn {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
