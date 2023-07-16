import React from "react";
import { ArrowCircleDown } from "./ArrowCircleDown";

export default {
  title: "ArrowCircleDown",
  component: ArrowCircleDown,
};

export const Default = () => <ArrowCircleDown />;
export const Fill = () => <ArrowCircleDown fill="blue" />;
export const Size = () => <ArrowCircleDown height="50" width="50" />;
export const CustomStyle = () => <ArrowCircleDown style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowCircleDown className="custom-class" />;
