import React from "react";
import { Visibility } from "./Visibility";

export default {
  title: "Visibility",
  component: Visibility,
};

export const Default = () => <Visibility />;
export const Fill = () => <Visibility fill="blue" />;
export const Size = () => <Visibility height="50" width="50" />;
export const CustomStyle = () => <Visibility style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Visibility className="custom-class" />;
