import React from "react";
import { TransitEnterExit } from "./TransitEnterExit";

export default {
  title: "TransitEnterExit",
  component: TransitEnterExit,
};

export const Default = () => <TransitEnterExit />;
export const Fill = () => <TransitEnterExit fill="blue" />;
export const Size = () => <TransitEnterExit height="50" width="50" />;
export const CustomStyle = () => <TransitEnterExit style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TransitEnterExit className="custom-class" />;
