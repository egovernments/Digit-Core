import React from "react";
import { ArrowDropDown } from "./ArrowDropDown";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowDropDown",
  component: ArrowDropDown,
};

export const Default = () => <ArrowDropDown />;
export const Fill = () => <ArrowDropDown fill="blue" />;
export const Size = () => <ArrowDropDown height="50" width="50" />;
export const CustomStyle = () => <ArrowDropDown style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowDropDown className="custom-class" />;
export const Clickable = () => <ArrowDropDown onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowDropDown {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
