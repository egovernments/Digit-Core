import React from "react";
import { Comment } from "./Comment";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Comment",
  component: Comment,
};

export const Default = () => <Comment />;
export const Fill = () => <Comment fill="blue" />;
export const Size = () => <Comment height="50" width="50" />;
export const CustomStyle = () => <Comment style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Comment className="custom-class" />;

export const Clickable = () => <Comment onClick={()=>console.log("clicked")} />;

const Template = (args) => <Comment {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
