import React from "react";
import { AddTask } from "./AddTask";

export default {
  title: "AddTask",
  component: AddTask,
};

export const Default = () => <AddTask />;
export const Fill = () => <AddTask fill="blue" />;
export const Size = () => <AddTask height="50" width="50" />;
export const CustomStyle = () => <AddTask style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddTask className="custom-class" />;
