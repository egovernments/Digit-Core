import React from "react";
import { ListAlt } from "./ListAlt";

export default {
  title: "ListAlt",
  component: ListAlt,
};

export const Default = () => <ListAlt />;
export const Fill = () => <ListAlt fill="blue" />;
export const Size = () => <ListAlt height="50" width="50" />;
export const CustomStyle = () => <ListAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ListAlt className="custom-class" />;
