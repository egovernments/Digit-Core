import React from "react";
import { CommentBank } from "./CommentBank";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CommentBank",
  component: CommentBank,
};

export const Default = () => <CommentBank />;
export const Fill = () => <CommentBank fill="blue" />;
export const Size = () => <CommentBank height="50" width="50" />;
export const CustomStyle = () => <CommentBank style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CommentBank className="custom-class" />;

export const Clickable = () => <CommentBank onClick={()=>console.log("clicked")} />;

const Template = (args) => <CommentBank {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
