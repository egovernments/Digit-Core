import React from "react";
import { DynamicForm } from "./DynamicForm";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DynamicForm",
  component: DynamicForm,
};

export const Default = () => <DynamicForm />;
export const Fill = () => <DynamicForm fill="blue" />;
export const Size = () => <DynamicForm height="50" width="50" />;
export const CustomStyle = () => <DynamicForm style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DynamicForm className="custom-class" />;

export const Clickable = () => <DynamicForm onClick={()=>console.log("clicked")} />;

const Template = (args) => <DynamicForm {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
