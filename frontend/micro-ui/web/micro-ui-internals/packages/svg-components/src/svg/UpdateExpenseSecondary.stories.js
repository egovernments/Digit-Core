import React from "react";
import { UpdateExpenseSecondary } from "./UpdateExpenseSecondary";

export default {
  title: "UpdateExpenseSecondary",
  component: UpdateExpenseSecondary,
};

export const Default = () => <UpdateExpenseSecondary />;
export const Fill = () => <UpdateExpenseSecondary fill="blue" />;
export const Size = () => <UpdateExpenseSecondary height="50" width="50" />;
export const CustomStyle = () => <UpdateExpenseSecondary style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <UpdateExpenseSecondary className="custom-class" />;
