import React from "react";
import { CreateNewFolder } from "./CreateNewFolder";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CreateNewFolder",
  component: CreateNewFolder,
};

export const Default = () => <CreateNewFolder />;
export const Fill = () => <CreateNewFolder fill="blue" />;
export const Size = () => <CreateNewFolder height="50" width="50" />;
export const CustomStyle = () => <CreateNewFolder style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CreateNewFolder className="custom-class" />;

export const Clickable = () => <CreateNewFolder onClick={()=>console.log("clicked")} />;

const Template = (args) => <CreateNewFolder {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
