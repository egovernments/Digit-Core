import React from "react";
import { SportsGolf } from "./SportsGolf";

export default {
  title: "SportsGolf",
  component: SportsGolf,
};

export const Default = () => <SportsGolf />;
export const Fill = () => <SportsGolf fill="blue" />;
export const Size = () => <SportsGolf height="50" width="50" />;
export const CustomStyle = () => <SportsGolf style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsGolf className="custom-class" />;
