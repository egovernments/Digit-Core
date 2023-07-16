import React from "react";
import { PausePresentation } from "./PausePresentation";

export default {
  title: "PausePresentation",
  component: PausePresentation,
};

export const Default = () => <PausePresentation />;
export const Fill = () => <PausePresentation fill="blue" />;
export const Size = () => <PausePresentation height="50" width="50" />;
export const CustomStyle = () => <PausePresentation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PausePresentation className="custom-class" />;
