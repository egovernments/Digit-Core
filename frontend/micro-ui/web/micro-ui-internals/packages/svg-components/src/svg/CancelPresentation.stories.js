import React from "react";
import { CancelPresentation } from "./CancelPresentation";

export default {
  title: "CancelPresentation",
  component: CancelPresentation,
};

export const Default = () => <CancelPresentation />;
export const Fill = () => <CancelPresentation fill="blue" />;
export const Size = () => <CancelPresentation height="50" width="50" />;
export const CustomStyle = () => <CancelPresentation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CancelPresentation className="custom-class" />;
