import React from "react";
import { Api } from "./Api";

export default {
  title: "Api",
  component: Api,
};

export const Default = () => <Api />;
export const Fill = () => <Api fill="blue" />;
export const Size = () => <Api height="50" width="50" />;
export const CustomStyle = () => <Api style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Api className="custom-class" />;
