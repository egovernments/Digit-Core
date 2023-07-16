import React from "react";
import { ForwardToInbox } from "./ForwardToInbox";

export default {
  title: "ForwardToInbox",
  component: ForwardToInbox,
};

export const Default = () => <ForwardToInbox />;
export const Fill = () => <ForwardToInbox fill="blue" />;
export const Size = () => <ForwardToInbox height="50" width="50" />;
export const CustomStyle = () => <ForwardToInbox style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ForwardToInbox className="custom-class" />;
