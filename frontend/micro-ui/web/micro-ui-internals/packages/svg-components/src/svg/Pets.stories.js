import React from "react";
import { Pets } from "./Pets";

export default {
  title: "Pets",
  component: Pets,
};

export const Default = () => <Pets />;
export const Fill = () => <Pets fill="blue" />;
export const Size = () => <Pets height="50" width="50" />;
export const CustomStyle = () => <Pets style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Pets className="custom-class" />;
