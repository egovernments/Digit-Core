import React from "react";
import { Navigation } from "./Navigation";

export default {
  title: "Navigation",
  component: Navigation,
};

export const Default = () => <Navigation />;
export const Fill = () => <Navigation fill="blue" />;
export const Size = () => <Navigation height="50" width="50" />;
export const CustomStyle = () => <Navigation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Navigation className="custom-class" />;
