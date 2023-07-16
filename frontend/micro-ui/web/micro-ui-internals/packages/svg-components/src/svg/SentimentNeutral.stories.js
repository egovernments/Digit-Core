import React from "react";
import { SentimentNeutral } from "./SentimentNeutral";

export default {
  title: "SentimentNeutral",
  component: SentimentNeutral,
};

export const Default = () => <SentimentNeutral />;
export const Fill = () => <SentimentNeutral fill="blue" />;
export const Size = () => <SentimentNeutral height="50" width="50" />;
export const CustomStyle = () => <SentimentNeutral style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SentimentNeutral className="custom-class" />;
