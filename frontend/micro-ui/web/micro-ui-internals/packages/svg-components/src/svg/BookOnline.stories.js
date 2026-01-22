import React from "react";
import { BookOnline } from "./BookOnline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "BookOnline",
  component: BookOnline,
};

export const Default = () => <BookOnline />;
export const Fill = () => <BookOnline fill="blue" />;
export const Size = () => <BookOnline height="50" width="50" />;
export const CustomStyle = () => <BookOnline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BookOnline className="custom-class" />;

export const Clickable = () => <BookOnline onClick={()=>console.log("clicked")} />;

const Template = (args) => <BookOnline {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
