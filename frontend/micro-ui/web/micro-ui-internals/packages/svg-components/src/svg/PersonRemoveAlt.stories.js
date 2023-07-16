import React from "react";
import { PersonRemoveAlt } from "./PersonRemoveAlt";

export default {
  title: "PersonRemoveAlt",
  component: PersonRemoveAlt,
};

export const Default = () => <PersonRemoveAlt />;
export const Fill = () => <PersonRemoveAlt fill="blue" />;
export const Size = () => <PersonRemoveAlt height="50" width="50" />;
export const CustomStyle = () => <PersonRemoveAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PersonRemoveAlt className="custom-class" />;
