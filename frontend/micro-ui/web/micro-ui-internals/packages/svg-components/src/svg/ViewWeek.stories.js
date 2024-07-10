import React from "react";
import { ViewWeek } from "./ViewWeek";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ViewWeek",
  component: ViewWeek,
};

export const Default = () => <ViewWeek />;
export const Fill = () => <ViewWeek fill="blue" />;
export const Size = () => <ViewWeek height="50" width="50" />;
export const CustomStyle = () => <ViewWeek style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewWeek className="custom-class" />;
export const Clickable = () => <ViewWeek onClick={()=>console.log("clicked")} />;

const Template = (args) => <ViewWeek {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
