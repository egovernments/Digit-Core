import React from "react";
import { Article } from "./Article";

export default {
  title: "Article",
  component: Article,
};

export const Default = () => <Article />;
export const Fill = () => <Article fill="blue" />;
export const Size = () => <Article height="50" width="50" />;
export const CustomStyle = () => <Article style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Article className="custom-class" />;
