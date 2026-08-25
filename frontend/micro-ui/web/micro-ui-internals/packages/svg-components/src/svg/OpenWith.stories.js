import React from "react";
import { OpenWith } from "./OpenWith";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "OpenWith",
  component: OpenWith,
};

export const Default = () => <OpenWith />;
export const Fill = () => <OpenWith fill="blue" />;
export const Size = () => <OpenWith height="50" width="50" />;
export const CustomStyle = () => <OpenWith style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <OpenWith className="custom-class" />;

export const Clickable = () => <OpenWith onClick={()=>console.log("clicked")} />;

const Template = (args) => <OpenWith {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
