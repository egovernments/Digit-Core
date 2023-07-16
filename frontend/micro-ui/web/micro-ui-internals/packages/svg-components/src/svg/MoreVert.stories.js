import React from "react";
import { MoreVert } from "./MoreVert";

export default {
  title: "MoreVert",
  component: MoreVert,
};

export const Default = () => <MoreVert />;
export const Fill = () => <MoreVert fill="blue" />;
export const Size = () => <MoreVert height="50" width="50" />;
export const CustomStyle = () => <MoreVert style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MoreVert className="custom-class" />;
