import React from "react";
import { UpdateExpense } from "./UpdateExpense";

export default {
  title: "UpdateExpense",
  component: UpdateExpense,
};

export const Default = () => <UpdateExpense />;
export const Fill = () => <UpdateExpense fill="blue" />;
export const Size = () => <UpdateExpense height="50" width="50" />;
export const CustomStyle = () => <UpdateExpense style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <UpdateExpense className="custom-class" />;
