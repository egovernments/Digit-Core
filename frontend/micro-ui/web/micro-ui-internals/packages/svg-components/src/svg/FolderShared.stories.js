import React from "react";
import { FolderShared } from "./FolderShared";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FolderShared",
  component: FolderShared,
};

export const Default = () => <FolderShared />;
export const Fill = () => <FolderShared fill="blue" />;
export const Size = () => <FolderShared height="50" width="50" />;
export const CustomStyle = () => <FolderShared style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FolderShared className="custom-class" />;

export const Clickable = () => <FolderShared onClick={()=>console.log("clicked")} />;

const Template = (args) => <FolderShared {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
