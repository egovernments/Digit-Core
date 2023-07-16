import React from "react";
import { AddExpenseTwo } from "./AddExpenseTwo";

export default {
  title: "AddExpenseTwo",
  component: AddExpenseTwo,
};

export const Default = () => <AddExpenseTwo />;
export const Fill = () => <AddExpenseTwo fill="blue" />;
export const Size = () => <AddExpenseTwo height="50" width="50" />;
export const CustomStyle = () => <AddExpenseTwo style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddExpenseTwo className="custom-class" />;
