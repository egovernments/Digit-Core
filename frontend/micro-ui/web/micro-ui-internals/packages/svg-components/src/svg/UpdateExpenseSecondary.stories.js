import React from "react";
import { UpdateExpenseSecondary } from "./UpdateExpenseSecondary";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "UpdateExpenseSecondary",
  component: UpdateExpenseSecondary,
};

export const Default = () => <UpdateExpenseSecondary />;
export const Fill = () => <UpdateExpenseSecondary fill="blue" />;
export const Size = () => <UpdateExpenseSecondary height="50" width="50" />;
export const CustomStyle = () => <UpdateExpenseSecondary style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <UpdateExpenseSecondary className="custom-class" />;
export const Clickable = () => <UpdateExpenseSecondary onClick={()=>console.log("clicked")} />;

const Template = (args) => <UpdateExpenseSecondary {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
