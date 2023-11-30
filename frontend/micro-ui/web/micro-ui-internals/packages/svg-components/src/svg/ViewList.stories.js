import React from "react";
import { ViewList } from "./ViewList";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ViewList",
  component: ViewList,
};

export const Default = () => <ViewList />;
export const Fill = () => <ViewList fill="blue" />;
export const Size = () => <ViewList height="50" width="50" />;
export const CustomStyle = () => <ViewList style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewList className="custom-class" />;
export const Clickable = () => <ViewList onClick={()=>console.log("clicked")} />;

const Template = (args) => <ViewList {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
