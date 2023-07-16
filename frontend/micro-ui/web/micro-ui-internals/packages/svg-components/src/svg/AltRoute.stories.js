import React from "react";
import { AltRoute } from "./AltRoute";

export default {
  title: "AltRoute",
  component: AltRoute,
};

export const Default = () => <AltRoute />;
export const Fill = () => <AltRoute fill="blue" />;
export const Size = () => <AltRoute height="50" width="50" />;
export const CustomStyle = () => <AltRoute style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AltRoute className="custom-class" />;
