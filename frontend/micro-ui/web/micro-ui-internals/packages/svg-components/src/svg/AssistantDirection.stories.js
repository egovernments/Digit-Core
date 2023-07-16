import React from "react";
import { AssistantDirection } from "./AssistantDirection";

export default {
  title: "AssistantDirection",
  component: AssistantDirection,
};

export const Default = () => <AssistantDirection />;
export const Fill = () => <AssistantDirection fill="blue" />;
export const Size = () => <AssistantDirection height="50" width="50" />;
export const CustomStyle = () => <AssistantDirection style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AssistantDirection className="custom-class" />;
