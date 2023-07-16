import React from "react";
import { PivotTableChart } from "./PivotTableChart";

export default {
  title: "PivotTableChart",
  component: PivotTableChart,
};

export const Default = () => <PivotTableChart />;
export const Fill = () => <PivotTableChart fill="blue" />;
export const Size = () => <PivotTableChart height="50" width="50" />;
export const CustomStyle = () => <PivotTableChart style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PivotTableChart className="custom-class" />;
