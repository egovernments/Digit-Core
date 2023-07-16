import React from "react";
import { ArrowBackIos } from "./ArrowBackIos";

export default {
  title: "ArrowBackIos",
  component: ArrowBackIos,
};

export const Default = () => <ArrowBackIos />;
export const Fill = () => <ArrowBackIos fill="blue" />;
export const Size = () => <ArrowBackIos height="50" width="50" />;
export const CustomStyle = () => <ArrowBackIos style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowBackIos className="custom-class" />;
