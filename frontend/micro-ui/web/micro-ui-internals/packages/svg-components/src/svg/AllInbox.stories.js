import React from "react";
import { AllInbox } from "./AllInbox";

export default {
  title: "AllInbox",
  component: AllInbox,
};

export const Default = () => <AllInbox />;
export const Fill = () => <AllInbox fill="blue" />;
export const Size = () => <AllInbox height="50" width="50" />;
export const CustomStyle = () => <AllInbox style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AllInbox className="custom-class" />;
