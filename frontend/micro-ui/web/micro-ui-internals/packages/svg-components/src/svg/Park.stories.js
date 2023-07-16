import React from "react";
import { Park } from "./Park";

export default {
  title: "Park",
  component: Park,
};

export const Default = () => <Park />;
export const Fill = () => <Park fill="blue" />;
export const Size = () => <Park height="50" width="50" />;
export const CustomStyle = () => <Park style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Park className="custom-class" />;
