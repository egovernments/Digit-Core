import React from "react";
import { FlipToFront } from "./FlipToFront";

export default {
  title: "FlipToFront",
  component: FlipToFront,
};

export const Default = () => <FlipToFront />;
export const Fill = () => <FlipToFront fill="blue" />;
export const Size = () => <FlipToFront height="50" width="50" />;
export const CustomStyle = () => <FlipToFront style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FlipToFront className="custom-class" />;
