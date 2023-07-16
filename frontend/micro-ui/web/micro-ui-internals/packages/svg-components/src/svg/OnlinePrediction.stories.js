import React from "react";
import { OnlinePrediction } from "./OnlinePrediction";

export default {
  title: "OnlinePrediction",
  component: OnlinePrediction,
};

export const Default = () => <OnlinePrediction />;
export const Fill = () => <OnlinePrediction fill="blue" />;
export const Size = () => <OnlinePrediction height="50" width="50" />;
export const CustomStyle = () => <OnlinePrediction style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <OnlinePrediction className="custom-class" />;
