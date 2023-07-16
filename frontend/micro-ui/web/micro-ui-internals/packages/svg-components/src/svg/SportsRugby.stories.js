import React from "react";
import { SportsRugby } from "./SportsRugby";

export default {
  title: "SportsRugby",
  component: SportsRugby,
};

export const Default = () => <SportsRugby />;
export const Fill = () => <SportsRugby fill="blue" />;
export const Size = () => <SportsRugby height="50" width="50" />;
export const CustomStyle = () => <SportsRugby style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsRugby className="custom-class" />;
