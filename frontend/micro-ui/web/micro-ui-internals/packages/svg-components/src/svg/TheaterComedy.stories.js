import React from "react";
import { TheaterComedy } from "./TheaterComedy";

export default {
  title: "TheaterComedy",
  component: TheaterComedy,
};

export const Default = () => <TheaterComedy />;
export const Fill = () => <TheaterComedy fill="blue" />;
export const Size = () => <TheaterComedy height="50" width="50" />;
export const CustomStyle = () => <TheaterComedy style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TheaterComedy className="custom-class" />;
