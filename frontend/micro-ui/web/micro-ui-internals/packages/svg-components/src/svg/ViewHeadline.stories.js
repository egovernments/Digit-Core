import React from "react";
import { ViewHeadline } from "./ViewHeadline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ViewHeadline",
  component: ViewHeadline,
};

export const Default = () => <ViewHeadline />;
export const Fill = () => <ViewHeadline fill="blue" />;
export const Size = () => <ViewHeadline height="50" width="50" />;
export const CustomStyle = () => <ViewHeadline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewHeadline className="custom-class" />;
export const Clickable = () => <ViewHeadline onClick={()=>console.log("clicked")} />;

const Template = (args) => <ViewHeadline {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
