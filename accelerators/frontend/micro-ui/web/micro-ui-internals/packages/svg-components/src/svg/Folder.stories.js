import React from "react";
import { Folder } from "./Folder";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Folder",
  component: Folder,
};

export const Default = () => <Folder />;
export const Fill = () => <Folder fill="blue" />;
export const Size = () => <Folder height="50" width="50" />;
export const CustomStyle = () => <Folder style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Folder className="custom-class" />;

export const Clickable = () => <Folder onClick={()=>console.log("clicked")} />;

const Template = (args) => <Folder {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
