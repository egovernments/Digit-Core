import React from "react";
import { SportsBaseball } from "./SportsBaseball";

export default {
  title: "SportsBaseball",
  component: SportsBaseball,
};

export const Default = () => <SportsBaseball />;
export const Fill = () => <SportsBaseball fill="blue" />;
export const Size = () => <SportsBaseball height="50" width="50" />;
export const CustomStyle = () => <SportsBaseball style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsBaseball className="custom-class" />;
