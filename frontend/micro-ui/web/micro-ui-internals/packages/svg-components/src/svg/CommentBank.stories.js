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
