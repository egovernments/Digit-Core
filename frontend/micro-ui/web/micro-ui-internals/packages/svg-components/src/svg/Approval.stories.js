import React from "react";
import { Approval } from "./Approval";

export default {
  title: "Approval",
  component: Approval,
};

export const Default = () => <Approval />;
export const Fill = () => <Approval fill="blue" />;
export const Size = () => <Approval height="50" width="50" />;
export const CustomStyle = () => <Approval style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Approval className="custom-class" />;
