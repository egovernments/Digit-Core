import React from "react";
import { TakeOutDining } from "./TakeOutDining";

export default {
  title: "TakeOutDining",
  component: TakeOutDining,
};

export const Default = () => <TakeOutDining />;
export const Fill = () => <TakeOutDining fill="blue" />;
export const Size = () => <TakeOutDining height="50" width="50" />;
export const CustomStyle = () => <TakeOutDining style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TakeOutDining className="custom-class" />;
