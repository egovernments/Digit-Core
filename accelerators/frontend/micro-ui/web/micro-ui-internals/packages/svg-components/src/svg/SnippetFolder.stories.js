import React from "react";
import { SnippetFolder } from "./SnippetFolder";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SnippetFolder",
  component: SnippetFolder,
};

export const Default = () => <SnippetFolder />;
export const Fill = () => <SnippetFolder fill="blue" />;
export const Size = () => <SnippetFolder height="50" width="50" />;
export const CustomStyle = () => <SnippetFolder style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SnippetFolder className="custom-class" />;

export const Clickable = () => <SnippetFolder onClick={()=>console.log("clicked")} />;

const Template = (args) => <SnippetFolder {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
