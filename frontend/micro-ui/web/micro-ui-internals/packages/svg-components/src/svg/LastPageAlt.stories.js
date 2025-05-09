import React from "react";
import { LastPageAlt } from "./LastPageAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LastPageAlt",
  component: LastPageAlt,
};

export const Default = () => <LastPageAlt />;
export const Fill = () => <LastPageAlt fill="blue" />;
export const Size = () => <LastPageAlt height="50" width="50" />;
export const CustomStyle = () => <LastPageAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LastPageAlt className="custom-class" />;

export const Clickable = () => <LastPageAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <LastPageAlt {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
