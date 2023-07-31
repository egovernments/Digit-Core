import React from "react";
import { FileUpload } from "./FileUpload";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FileUpload",
  component: FileUpload,
};

export const Default = () => <FileUpload />;
export const Fill = () => <FileUpload fill="blue" />;
export const Size = () => <FileUpload height="50" width="50" />;
export const CustomStyle = () => <FileUpload style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FileUpload className="custom-class" />;

export const Clickable = () => <FileUpload onClick={()=>console.log("clicked")} />;

const Template = (args) => <FileUpload {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
