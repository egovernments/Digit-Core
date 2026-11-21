import React from "react";
import { ArrowLeft } from "./ArrowLeft";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowLeft",
  component: ArrowLeft,
};

export const Default = () => <ArrowLeft />;
export const Fill = () => <ArrowLeft fill="blue" />;
export const Size = () => <ArrowLeft height="50" width="50" />;
export const CustomStyle = () => <ArrowLeft style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowLeft className="custom-class" />;
export const Clickable = () => <ArrowLeft onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowLeft {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
