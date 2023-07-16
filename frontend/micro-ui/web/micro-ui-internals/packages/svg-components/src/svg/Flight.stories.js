import React from "react";
import { Flight } from "./Flight";

export default {
  title: "Flight",
  component: Flight,
};

export const Default = () => <Flight />;
export const Fill = () => <Flight fill="blue" />;
export const Size = () => <Flight height="50" width="50" />;
export const CustomStyle = () => <Flight style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Flight className="custom-class" />;
