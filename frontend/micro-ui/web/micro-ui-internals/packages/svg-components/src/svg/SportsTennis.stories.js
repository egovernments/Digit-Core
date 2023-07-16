import React from "react";
import { SportsTennis } from "./SportsTennis";

export default {
  title: "SportsTennis",
  component: SportsTennis,
};

export const Default = () => <SportsTennis />;
export const Fill = () => <SportsTennis fill="blue" />;
export const Size = () => <SportsTennis height="50" width="50" />;
export const CustomStyle = () => <SportsTennis style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsTennis className="custom-class" />;
