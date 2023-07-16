import React from "react";
import { SentimentVeryDissatisfied } from "./SentimentVeryDissatisfied";

export default {
  title: "SentimentVeryDissatisfied",
  component: SentimentVeryDissatisfied,
};

export const Default = () => <SentimentVeryDissatisfied />;
export const Fill = () => <SentimentVeryDissatisfied fill="blue" />;
export const Size = () => <SentimentVeryDissatisfied height="50" width="50" />;
export const CustomStyle = () => <SentimentVeryDissatisfied style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SentimentVeryDissatisfied className="custom-class" />;
