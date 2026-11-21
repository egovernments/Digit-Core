import React from "react";
import { AddTask } from "./AddTask";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AddTask",
  component: AddTask,
};

export const Default = () => <AddTask />;
export const Fill = () => <AddTask fill="blue" />;
export const Size = () => <AddTask height="50" width="50" />;
export const CustomStyle = () => <AddTask style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddTask className="custom-class" />;
export const Clickable = () => <AddTask onClick={()=>console.log("clicked")} />;

const Template = (args) => <AddTask {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
