import React from "react";
import { DynamicForm } from "./DynamicForm";

export default {
  title: "DynamicForm",
  component: DynamicForm,
};

export const Default = () => <DynamicForm />;
export const Fill = () => <DynamicForm fill="blue" />;
export const Size = () => <DynamicForm height="50" width="50" />;
export const CustomStyle = () => <DynamicForm style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DynamicForm className="custom-class" />;
