import React from "react";
import { AssignmentReturned } from "./AssignmentReturned";

export default {
  title: "AssignmentReturned",
  component: AssignmentReturned,
};

export const Default = () => <AssignmentReturned />;
export const Fill = () => <AssignmentReturned fill="blue" />;
export const Size = () => <AssignmentReturned height="50" width="50" />;
export const CustomStyle = () => <AssignmentReturned style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AssignmentReturned className="custom-class" />;
