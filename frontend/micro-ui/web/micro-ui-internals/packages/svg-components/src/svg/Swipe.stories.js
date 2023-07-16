import React from "react";
import { Swipe } from "./Swipe";

export default {
  title: "Swipe",
  component: Swipe,
};

export const Default = () => <Swipe />;
export const Fill = () => <Swipe fill="blue" />;
export const Size = () => <Swipe height="50" width="50" />;
export const CustomStyle = () => <Swipe style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Swipe className="custom-class" />;
