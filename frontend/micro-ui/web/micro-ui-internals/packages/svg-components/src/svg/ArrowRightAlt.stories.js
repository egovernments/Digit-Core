import React from "react";
import { ArrowRightAlt } from "./ArrowRightAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowRightAlt",
  component: ArrowRightAlt,
};

export const Default = () => <ArrowRightAlt />;
export const Fill = () => <ArrowRightAlt fill="blue" />;
export const Size = () => <ArrowRightAlt height="50" width="50" />;
export const CustomStyle = () => <ArrowRightAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowRightAlt className="custom-class" />;
export const Clickable = () => <ArrowRightAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowRightAlt {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
