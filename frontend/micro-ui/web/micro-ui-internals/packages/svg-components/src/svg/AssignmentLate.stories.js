import React from "react";
import { AssignmentLate } from "./AssignmentLate";

export default {
  title: "AssignmentLate",
  component: AssignmentLate,
};

export const Default = () => <AssignmentLate />;
export const Fill = () => <AssignmentLate fill="blue" />;
export const Size = () => <AssignmentLate height="50" width="50" />;
export const CustomStyle = () => <AssignmentLate style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AssignmentLate className="custom-class" />;
