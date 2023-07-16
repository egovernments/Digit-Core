import React from "react";
import { LocalLibrary } from "./LocalLibrary";

export default {
  title: "LocalLibrary",
  component: LocalLibrary,
};

export const Default = () => <LocalLibrary />;
export const Fill = () => <LocalLibrary fill="blue" />;
export const Size = () => <LocalLibrary height="50" width="50" />;
export const CustomStyle = () => <LocalLibrary style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalLibrary className="custom-class" />;
