import React from "react";
import { ArrowForwardIosAlt } from "./ArrowForwardIosAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowForwardIosAlt",
  component: ArrowForwardIosAlt,
};

export const Default = () => <ArrowForwardIosAlt />;
export const Fill = () => <ArrowForwardIosAlt fill="blue" />;
export const Size = () => <ArrowForwardIosAlt height="50" width="50" />;
export const CustomStyle = () => <ArrowForwardIosAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowForwardIosAlt className="custom-class" />;
export const Clickable = () => <ArrowForwardIosAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowForwardIosAlt {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
