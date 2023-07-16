import React from "react";
import { SportsFootball } from "./SportsFootball";

export default {
  title: "SportsFootball",
  component: SportsFootball,
};

export const Default = () => <SportsFootball />;
export const Fill = () => <SportsFootball fill="blue" />;
export const Size = () => <SportsFootball height="50" width="50" />;
export const CustomStyle = () => <SportsFootball style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsFootball className="custom-class" />;
