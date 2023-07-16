import React from "react";
import { DirectionsRailway } from "./DirectionsRailway";

export default {
  title: "DirectionsRailway",
  component: DirectionsRailway,
};

export const Default = () => <DirectionsRailway />;
export const Fill = () => <DirectionsRailway fill="blue" />;
export const Size = () => <DirectionsRailway height="50" width="50" />;
export const CustomStyle = () => <DirectionsRailway style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DirectionsRailway className="custom-class" />;
