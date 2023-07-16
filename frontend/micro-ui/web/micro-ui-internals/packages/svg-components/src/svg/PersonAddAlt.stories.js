import React from "react";
import { PersonAddAlt } from "./PersonAddAlt";

export default {
  title: "PersonAddAlt",
  component: PersonAddAlt,
};

export const Default = () => <PersonAddAlt />;
export const Fill = () => <PersonAddAlt fill="blue" />;
export const Size = () => <PersonAddAlt height="50" width="50" />;
export const CustomStyle = () => <PersonAddAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PersonAddAlt className="custom-class" />;
