import React from "react";
import { AddChart } from "./AddChart";

export default {
  title: "AddChart",
  component: AddChart,
};

export const Default = () => <AddChart />;
export const Fill = () => <AddChart fill="blue" />;
export const Size = () => <AddChart height="50" width="50" />;
export const CustomStyle = () => <AddChart style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddChart className="custom-class" />;
