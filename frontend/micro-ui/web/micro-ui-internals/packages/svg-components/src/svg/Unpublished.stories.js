import React from "react";
import { Unpublished } from "./Unpublished";

export default {
  title: "Unpublished",
  component: Unpublished,
};

export const Default = () => <Unpublished />;
export const Fill = () => <Unpublished fill="blue" />;
export const Size = () => <Unpublished height="50" width="50" />;
export const CustomStyle = () => <Unpublished style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Unpublished className="custom-class" />;
