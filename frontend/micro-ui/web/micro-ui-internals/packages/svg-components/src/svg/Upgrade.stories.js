import React from "react";
import { Upgrade } from "./Upgrade";

export default {
  title: "Upgrade",
  component: Upgrade,
};

export const Default = () => <Upgrade />;
export const Fill = () => <Upgrade fill="blue" />;
export const Size = () => <Upgrade height="50" width="50" />;
export const CustomStyle = () => <Upgrade style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Upgrade className="custom-class" />;
