import React from "react";
import { BookmarkBorder } from "./BookmarkBorder";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "BookmarkBorder",
  component: BookmarkBorder,
};

export const Default = () => <BookmarkBorder />;
export const Fill = () => <BookmarkBorder fill="blue" />;
export const Size = () => <BookmarkBorder height="50" width="50" />;
export const CustomStyle = () => <BookmarkBorder style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BookmarkBorder className="custom-class" />;

export const Clickable = () => <BookmarkBorder onClick={()=>console.log("clicked")} />;

const Template = (args) => <BookmarkBorder {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
