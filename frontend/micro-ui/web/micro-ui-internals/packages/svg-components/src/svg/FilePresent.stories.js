import React from "react";
import { FilePresent } from "./FilePresent";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FilePresent",
  component: FilePresent,
};

export const Default = () => <FilePresent />;
export const Fill = () => <FilePresent fill="blue" />;
export const Size = () => <FilePresent height="50" width="50" />;
export const CustomStyle = () => <FilePresent style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FilePresent className="custom-class" />;

export const Clickable = () => <FilePresent onClick={()=>console.log("clicked")} />;

const Template = (args) => <FilePresent {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
