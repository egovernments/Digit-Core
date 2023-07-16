import React from "react";
import { AddExpense } from "./AddExpense";

export default {
  title: "AddExpense",
  component: AddExpense,
};

export const Default = () => <AddExpense />;
export const Fill = () => <AddExpense fill="blue" />;
export const Size = () => <AddExpense height="50" width="50" />;
export const CustomStyle = () => <AddExpense style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddExpense className="custom-class" />;
