import React from "react";
import { AddLocationAlt } from "./AddLocationAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AddLocationAlt",
  component: AddLocationAlt,
};

export const Default = () => <AddLocationAlt />;
export const Fill = () => <AddLocationAlt fill="blue" />;
export const Size = () => <AddLocationAlt height="50" width="50" />;
export const CustomStyle = () => <AddLocationAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddLocationAlt className="custom-class" />;
export const Clickable = () => <AddLocationAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <AddLocationAlt {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
