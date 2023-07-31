import React from "react";
import { FolderOpen } from "./FolderOpen";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FolderOpen",
  component: FolderOpen,
};

export const Default = () => <FolderOpen />;
export const Fill = () => <FolderOpen fill="blue" />;
export const Size = () => <FolderOpen height="50" width="50" />;
export const CustomStyle = () => <FolderOpen style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FolderOpen className="custom-class" />;

export const Clickable = () => <FolderOpen onClick={()=>console.log("clicked")} />;

const Template = (args) => <FolderOpen {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
