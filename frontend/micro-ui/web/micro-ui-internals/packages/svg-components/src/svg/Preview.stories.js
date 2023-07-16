import React from "react";
import { Preview } from "./Preview";

export default {
  title: "Preview",
  component: Preview,
};

export const Default = () => <Preview />;
export const Fill = () => <Preview fill="blue" />;
export const Size = () => <Preview height="50" width="50" />;
export const CustomStyle = () => <Preview style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Preview className="custom-class" />;
