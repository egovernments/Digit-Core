import React from "react";
import { DisabledByDefault } from "./DisabledByDefault";

export default {
  title: "DisabledByDefault",
  component: DisabledByDefault,
};

export const Default = () => <DisabledByDefault />;
export const Fill = () => <DisabledByDefault fill="blue" />;
export const Size = () => <DisabledByDefault height="50" width="50" />;
export const CustomStyle = () => <DisabledByDefault style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DisabledByDefault className="custom-class" />;
