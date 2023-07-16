import React from "react";
import { ArrowForwardIos } from "./ArrowForwardIos";

export default {
  title: "ArrowForwardIos",
  component: ArrowForwardIos,
};

export const Default = () => <ArrowForwardIos />;
export const Fill = () => <ArrowForwardIos fill="blue" />;
export const Size = () => <ArrowForwardIos height="50" width="50" />;
export const CustomStyle = () => <ArrowForwardIos style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowForwardIos className="custom-class" />;
