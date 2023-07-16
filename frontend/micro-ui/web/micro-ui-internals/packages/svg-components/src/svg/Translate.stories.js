import React from "react";
import { Translate } from "./Translate";

export default {
  title: "Translate",
  component: Translate,
};

export const Default = () => <Translate />;
export const Fill = () => <Translate fill="blue" />;
export const Size = () => <Translate height="50" width="50" />;
export const CustomStyle = () => <Translate style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Translate className="custom-class" />;
