import React from "react";
import { AddLocationAlt } from "./AddLocationAlt";

export default {
  title: "AddLocationAlt",
  component: AddLocationAlt,
};

export const Default = () => <AddLocationAlt />;
export const Fill = () => <AddLocationAlt fill="blue" />;
export const Size = () => <AddLocationAlt height="50" width="50" />;
export const CustomStyle = () => <AddLocationAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddLocationAlt className="custom-class" />;
