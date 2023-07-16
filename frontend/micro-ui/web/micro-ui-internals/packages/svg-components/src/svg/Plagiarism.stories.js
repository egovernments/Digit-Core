import React from "react";
import { Plagiarism } from "./Plagiarism";

export default {
  title: "Plagiarism",
  component: Plagiarism,
};

export const Default = () => <Plagiarism />;
export const Fill = () => <Plagiarism fill="blue" />;
export const Size = () => <Plagiarism height="50" width="50" />;
export const CustomStyle = () => <Plagiarism style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Plagiarism className="custom-class" />;
