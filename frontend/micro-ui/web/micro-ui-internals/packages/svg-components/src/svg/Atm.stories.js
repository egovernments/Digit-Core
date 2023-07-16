import React from "react";
import { Atm } from "./Atm";

export default {
  title: "Atm",
  component: Atm,
};

export const Default = () => <Atm />;
export const Fill = () => <Atm fill="blue" />;
export const Size = () => <Atm height="50" width="50" />;
export const CustomStyle = () => <Atm style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Atm className="custom-class" />;
