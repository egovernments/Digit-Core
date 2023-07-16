import React from "react";
import { WaterfallChart } from "./WaterfallChart";

export default {
  title: "WaterfallChart",
  component: WaterfallChart,
};

export const Default = () => <WaterfallChart />;
export const Fill = () => <WaterfallChart fill="blue" />;
export const Size = () => <WaterfallChart height="50" width="50" />;
export const CustomStyle = () => <WaterfallChart style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WaterfallChart className="custom-class" />;
