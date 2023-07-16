import React from "react";
import { SentimentDissatisfied } from "./SentimentDissatisfied";

export default {
  title: "SentimentDissatisfied",
  component: SentimentDissatisfied,
};

export const Default = () => <SentimentDissatisfied />;
export const Fill = () => <SentimentDissatisfied fill="blue" />;
export const Size = () => <SentimentDissatisfied height="50" width="50" />;
export const CustomStyle = () => <SentimentDissatisfied style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SentimentDissatisfied className="custom-class" />;
