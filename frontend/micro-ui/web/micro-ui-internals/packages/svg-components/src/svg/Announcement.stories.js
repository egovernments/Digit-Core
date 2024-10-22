import React from "react";
import { Announcement } from "./Announcement";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Announcement",
  component: Announcement,
};

export const Default = () => <Announcement />;
export const Fill = () => <Announcement fill="blue" />;
export const Size = () => <Announcement height="50" width="50" />;
export const CustomStyle = () => <Announcement style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Announcement className="custom-class" />;
export const Clickable = () => <Announcement onClick={()=>console.log("clicked")} />;

const Template = (args) => <Announcement {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
