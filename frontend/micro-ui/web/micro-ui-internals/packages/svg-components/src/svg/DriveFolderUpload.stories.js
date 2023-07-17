import React from "react";
import { DriveFolderUpload } from "./DriveFolderUpload";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DriveFolderUpload",
  component: DriveFolderUpload,
};

export const Default = () => <DriveFolderUpload />;
export const Fill = () => <DriveFolderUpload fill="blue" />;
export const Size = () => <DriveFolderUpload height="50" width="50" />;
export const CustomStyle = () => <DriveFolderUpload style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DriveFolderUpload className="custom-class" />;

export const Clickable = () => <DriveFolderUpload onClick={()=>console.log("clicked")} />;

const Template = (args) => <DriveFolderUpload {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
