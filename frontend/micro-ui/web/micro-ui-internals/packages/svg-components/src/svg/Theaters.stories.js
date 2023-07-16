import React from "react";
import { Theaters } from "./Theaters";

export default {
  title: "Theaters",
  component: Theaters,
};

export const Default = () => <Theaters />;
export const Fill = () => <Theaters fill="blue" />;
export const Size = () => <Theaters height="50" width="50" />;
export const CustomStyle = () => <Theaters style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Theaters className="custom-class" />;
