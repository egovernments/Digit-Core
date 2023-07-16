import React from "react";
import { East } from "./East";

export default {
  title: "East",
  component: East,
};

export const Default = () => <East />;
export const Fill = () => <East fill="blue" />;
export const Size = () => <East height="50" width="50" />;
export const CustomStyle = () => <East style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <East className="custom-class" />;
