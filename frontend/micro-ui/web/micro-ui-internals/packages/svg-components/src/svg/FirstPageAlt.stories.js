import React from "react";
import { FirstPageAlt } from "./FirstPageAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FirstPageAlt",
  component: FirstPageAlt,
};

export const Default = () => <FirstPageAlt />;
export const Fill = () => <FirstPageAlt fill="blue" />;
export const Size = () => <FirstPageAlt height="50" width="50" />;
export const CustomStyle = () => <FirstPageAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FirstPageAlt className="custom-class" />;

export const Clickable = () => <FirstPageAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <FirstPageAlt {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
