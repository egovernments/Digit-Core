import React from "react";
import { FileDownload } from "./FileDownload";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FileDownload",
  component: FileDownload,
};

export const Default = () => <FileDownload />;
export const Fill = () => <FileDownload fill="blue" />;
export const Size = () => <FileDownload height="50" width="50" />;
export const CustomStyle = () => <FileDownload style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FileDownload className="custom-class" />;

export const Clickable = () => <FileDownload onClick={()=>console.log("clicked")} />;

const Template = (args) => <FileDownload {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
