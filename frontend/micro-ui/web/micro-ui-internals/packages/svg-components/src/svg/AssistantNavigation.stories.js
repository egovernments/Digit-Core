import React from "react";
import { AssistantNavigation } from "./AssistantNavigation";

export default {
  title: "AssistantNavigation",
  component: AssistantNavigation,
};

export const Default = () => <AssistantNavigation />;
export const Fill = () => <AssistantNavigation fill="blue" />;
export const Size = () => <AssistantNavigation height="50" width="50" />;
export const CustomStyle = () => <AssistantNavigation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AssistantNavigation className="custom-class" />;
