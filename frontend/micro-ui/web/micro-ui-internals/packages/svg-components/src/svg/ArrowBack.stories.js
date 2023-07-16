import React from "react";
import { ArrowBack } from "./ArrowBack";

export default {
  title: "ArrowBack",
  component: ArrowBack,
};

export const Default = () => <ArrowBack />;
export const Fill = () => <ArrowBack fill="blue" />;
export const Size = () => <ArrowBack height="50" width="50" />;
export const CustomStyle = () => <ArrowBack style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowBack className="custom-class" />;
