import React from "react";
import { UploadFile } from "./UploadFile";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "UploadFile",
  component: UploadFile,
};

export const Default = () => <UploadFile />;
export const Fill = () => <UploadFile fill="blue" />;
export const Size = () => <UploadFile height="50" width="50" />;
export const CustomStyle = () => <UploadFile style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <UploadFile className="custom-class" />;
export const Clickable = () => <UploadFile onClick={()=>console.log("clicked")} />;

const Template = (args) => <UploadFile {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
