import React from "react";
import { AssignmentTurnedIn } from "./AssignmentTurnedIn";

export default {
  title: "AssignmentTurnedIn",
  component: AssignmentTurnedIn,
};

export const Default = () => <AssignmentTurnedIn />;
export const Fill = () => <AssignmentTurnedIn fill="blue" />;
export const Size = () => <AssignmentTurnedIn height="50" width="50" />;
export const CustomStyle = () => <AssignmentTurnedIn style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AssignmentTurnedIn className="custom-class" />;
