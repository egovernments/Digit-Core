import React from "react";
import { File } from "./File";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "File",
  component: File,
};

export const Default = () => <File />;
export const Fill = () => <File fill="blue" />;
export const Size = () => <File height="50" width="50" />;
export const CustomStyle = () => <File style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <File className="custom-class" />;

export const Clickable = () => <File onClick={()=>console.log("clicked")} />;

const Template = (args) => <File {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
