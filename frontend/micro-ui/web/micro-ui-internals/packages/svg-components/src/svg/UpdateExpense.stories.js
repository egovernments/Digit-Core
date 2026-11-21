import React from "react";
import { UpdateExpense } from "./UpdateExpense";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "UpdateExpense",
  component: UpdateExpense,
};

export const Default = () => <UpdateExpense />;
export const Fill = () => <UpdateExpense fill="blue" />;
export const Size = () => <UpdateExpense height="50" width="50" />;
export const CustomStyle = () => <UpdateExpense style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <UpdateExpense className="custom-class" />;
export const Clickable = () => <UpdateExpense onClick={()=>console.log("clicked")} />;

const Template = (args) => <UpdateExpense {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
