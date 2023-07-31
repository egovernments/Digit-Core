import React from "react";
import { CloudUpload } from "./CloudUpload";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CloudUpload",
  component: CloudUpload,
};

export const Default = () => <CloudUpload />;
export const Fill = () => <CloudUpload fill="blue" />;
export const Size = () => <CloudUpload height="50" width="50" />;
export const CustomStyle = () => <CloudUpload style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CloudUpload className="custom-class" />;

export const Clickable = () => <CloudUpload onClick={()=>console.log("clicked")} />;

const Template = (args) => <CloudUpload {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
