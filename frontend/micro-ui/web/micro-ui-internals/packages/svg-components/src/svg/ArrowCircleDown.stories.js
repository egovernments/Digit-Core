import React from "react";
import { ArrowCircleDown } from "./ArrowCircleDown";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowCircleDown",
  component: ArrowCircleDown,
};

export const Default = () => <ArrowCircleDown />;
export const Fill = () => <ArrowCircleDown fill="blue" />;
export const Size = () => <ArrowCircleDown height="50" width="50" />;
export const CustomStyle = () => <ArrowCircleDown style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowCircleDown className="custom-class" />;
export const Clickable = () => <ArrowCircleDown onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowCircleDown {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
