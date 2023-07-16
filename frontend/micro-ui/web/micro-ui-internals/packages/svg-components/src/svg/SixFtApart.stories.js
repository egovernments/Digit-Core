import React from "react";
import { SixFtApart } from "./SixFtApart";

export default {
  title: "SixFtApart",
  component: SixFtApart,
};

export const Default = () => <SixFtApart />;
export const Fill = () => <SixFtApart fill="blue" />;
export const Size = () => <SixFtApart height="50" width="50" />;
export const CustomStyle = () => <SixFtApart style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SixFtApart className="custom-class" />;
