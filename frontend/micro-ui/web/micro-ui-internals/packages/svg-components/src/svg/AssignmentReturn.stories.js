import React from "react";
import { AssignmentReturn } from "./AssignmentReturn";

export default {
  title: "AssignmentReturn",
  component: AssignmentReturn,
};

export const Default = () => <AssignmentReturn />;
export const Fill = () => <AssignmentReturn fill="blue" />;
export const Size = () => <AssignmentReturn height="50" width="50" />;
export const CustomStyle = () => <AssignmentReturn style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AssignmentReturn className="custom-class" />;
