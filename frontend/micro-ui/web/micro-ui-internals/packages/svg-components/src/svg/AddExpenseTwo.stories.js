import React from "react";
import { AddExpenseTwo } from "./AddExpenseTwo";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AddExpenseTwo",
  component: AddExpenseTwo,
};

export const Default = () => <AddExpenseTwo />;
export const Fill = () => <AddExpenseTwo fill="blue" />;
export const Size = () => <AddExpenseTwo height="50" width="50" />;
export const CustomStyle = () => <AddExpenseTwo style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddExpenseTwo className="custom-class" />;
export const Clickable = () => <AddExpenseTwo onClick={()=>console.log("clicked")} />;

const Template = (args) => <AddExpenseTwo {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
