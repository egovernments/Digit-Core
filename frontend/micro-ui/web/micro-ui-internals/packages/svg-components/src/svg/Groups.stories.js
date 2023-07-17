import React from "react";
import { Groups } from "./Groups";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Groups",
  component: Groups,
};

export const Default = () => <Groups />;
export const Fill = () => <Groups fill="blue" />;
export const Size = () => <Groups height="50" width="50" />;
export const CustomStyle = () => <Groups style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Groups className="custom-class" />;

export const Clickable = () => <Groups onClick={()=>console.log("clicked")} />;

const Template = (args) => <Groups {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
