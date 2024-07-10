import React from "react";
import { AddExpense } from "./AddExpense";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AddExpense",
  component: AddExpense,
};

export const Default = () => <AddExpense />;
export const Fill = () => <AddExpense fill="blue" />;
export const Size = () => <AddExpense height="50" width="50" />;
export const CustomStyle = () => <AddExpense style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddExpense className="custom-class" />;
export const Clickable = () => <AddExpense onClick={()=>console.log("clicked")} />;

const Template = (args) => <AddExpense {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
