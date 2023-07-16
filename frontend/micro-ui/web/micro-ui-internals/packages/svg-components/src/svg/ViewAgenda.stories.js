import React from "react";
import { ViewAgenda } from "./ViewAgenda";

export default {
  title: "ViewAgenda",
  component: ViewAgenda,
};

export const Default = () => <ViewAgenda />;
export const Fill = () => <ViewAgenda fill="blue" />;
export const Size = () => <ViewAgenda height="50" width="50" />;
export const CustomStyle = () => <ViewAgenda style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewAgenda className="custom-class" />;
