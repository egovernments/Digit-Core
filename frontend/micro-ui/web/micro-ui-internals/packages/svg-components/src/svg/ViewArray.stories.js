import React from "react";
import { ViewArray } from "./ViewArray";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ViewArray",
  component: ViewArray,
};

export const Default = () => <ViewArray />;
export const Fill = () => <ViewArray fill="blue" />;
export const Size = () => <ViewArray height="50" width="50" />;
export const CustomStyle = () => <ViewArray style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewArray className="custom-class" />;
export const Clickable = () => <ViewArray onClick={()=>console.log("clicked")} />;

const Template = (args) => <ViewArray {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
