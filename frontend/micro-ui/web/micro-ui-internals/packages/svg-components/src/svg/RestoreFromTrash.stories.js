import React from "react";
import { RestoreFromTrash } from "./RestoreFromTrash";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RestoreFromTrash",
  component: RestoreFromTrash,
};

export const Default = () => <RestoreFromTrash />;
export const Fill = () => <RestoreFromTrash fill="blue" />;
export const Size = () => <RestoreFromTrash height="50" width="50" />;
export const CustomStyle = () => <RestoreFromTrash style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RestoreFromTrash className="custom-class" />;

export const Clickable = () => <RestoreFromTrash onClick={()=>console.log("clicked")} />;

const Template = (args) => <RestoreFromTrash {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
