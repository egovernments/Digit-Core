import React from "react";
import { AddToDrive } from "./AddToDrive";

export default {
  title: "AddToDrive",
  component: AddToDrive,
};

export const Default = () => <AddToDrive />;
export const Fill = () => <AddToDrive fill="blue" />;
export const Size = () => <AddToDrive height="50" width="50" />;
export const CustomStyle = () => <AddToDrive style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddToDrive className="custom-class" />;
