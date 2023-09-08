import React from "react";
import { Bookmarks } from "./Bookmarks";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Bookmarks",
  component: Bookmarks,
};

export const Default = () => <Bookmarks />;
export const Fill = () => <Bookmarks fill="blue" />;
export const Size = () => <Bookmarks height="50" width="50" />;
export const CustomStyle = () => <Bookmarks style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Bookmarks className="custom-class" />;

export const Clickable = () => <Bookmarks onClick={()=>console.log("clicked")} />;

const Template = (args) => <Bookmarks {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
