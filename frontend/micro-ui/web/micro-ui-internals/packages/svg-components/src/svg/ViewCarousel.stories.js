import React from "react";
import { ViewCarousel } from "./ViewCarousel";

export default {
  title: "ViewCarousel",
  component: ViewCarousel,
};

export const Default = () => <ViewCarousel />;
export const Fill = () => <ViewCarousel fill="blue" />;
export const Size = () => <ViewCarousel height="50" width="50" />;
export const CustomStyle = () => <ViewCarousel style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewCarousel className="custom-class" />;
