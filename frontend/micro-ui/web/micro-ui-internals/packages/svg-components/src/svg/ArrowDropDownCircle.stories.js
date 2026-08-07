import React from "react";
import { ArrowDropDownCircle } from "./ArrowDropDownCircle";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowDropDownCircle",
  component: ArrowDropDownCircle,
};

export const Default = () => <ArrowDropDownCircle />;
export const Fill = () => <ArrowDropDownCircle fill="blue" />;
export const Size = () => <ArrowDropDownCircle height="50" width="50" />;
export const CustomStyle = () => <ArrowDropDownCircle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowDropDownCircle className="custom-class" />;
export const Clickable = () => <ArrowDropDownCircle onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowDropDownCircle {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
