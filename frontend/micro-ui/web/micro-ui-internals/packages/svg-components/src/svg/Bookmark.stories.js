import React from "react";
import { Bookmark } from "./Bookmark";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Bookmark",
  component: Bookmark,
};

export const Default = () => <Bookmark />;
export const Fill = () => <Bookmark fill="blue" />;
export const Size = () => <Bookmark height="50" width="50" />;
export const CustomStyle = () => <Bookmark style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Bookmark className="custom-class" />;

export const Clickable = () => <Bookmark onClick={()=>console.log("clicked")} />;

const Template = (args) => <Bookmark {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
