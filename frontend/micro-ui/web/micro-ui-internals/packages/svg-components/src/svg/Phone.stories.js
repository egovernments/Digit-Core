import React from "react";
import { Phone } from "./Phone";

export default {
  title: "Phone",
  component: Phone,
};

export const Default = () => <Phone />;
export const Fill = () => <Phone fill="blue" />;
export const Size = () => <Phone height="50" width="50" />;
export const CustomStyle = () => <Phone style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Phone className="custom-class" />;
