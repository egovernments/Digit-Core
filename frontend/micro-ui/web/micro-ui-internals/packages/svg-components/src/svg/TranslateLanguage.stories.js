import React from "react";
import { TranslateLanguage } from "./TranslateLanguage";

export default {
  title: "TranslateLanguage",
  component: TranslateLanguage,
};

export const Default = () => <TranslateLanguage />;
export const Fill = () => <TranslateLanguage fill="blue" />;
export const Size = () => <TranslateLanguage height="50" width="50" />;
export const CustomStyle = () => <TranslateLanguage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TranslateLanguage className="custom-class" />;
