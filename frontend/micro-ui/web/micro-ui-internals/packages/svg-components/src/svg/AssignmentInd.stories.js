import React from "react";
import { AssignmentInd } from "./AssignmentInd";

export default {
  title: "AssignmentInd",
  component: AssignmentInd,
};

export const Default = () => <AssignmentInd />;
export const Fill = () => <AssignmentInd fill="blue" />;
export const Size = () => <AssignmentInd height="50" width="50" />;
export const CustomStyle = () => <AssignmentInd style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AssignmentInd className="custom-class" />;
