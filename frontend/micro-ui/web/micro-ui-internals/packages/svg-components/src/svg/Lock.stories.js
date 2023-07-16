import React from "react";
import { Lock } from "./Lock";

export default {
  title: "Lock",
  component: Lock,
};

export const Default = () => <Lock />;
export const Fill = () => <Lock fill="blue" />;
export const Size = () => <Lock height="50" width="50" />;
export const CustomStyle = () => <Lock style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Lock className="custom-class" />;
