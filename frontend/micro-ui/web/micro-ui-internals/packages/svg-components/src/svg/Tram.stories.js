import React from "react";
import { Tram } from "./Tram";

export default {
  title: "Tram",
  component: Tram,
};

export const Default = () => <Tram />;
export const Fill = () => <Tram fill="blue" />;
export const Size = () => <Tram height="50" width="50" />;
export const CustomStyle = () => <Tram style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Tram className="custom-class" />;
