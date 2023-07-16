import React from "react";
import { Description } from "./Description";

export default {
  title: "Description",
  component: Description,
};

export const Default = () => <Description />;
export const Fill = () => <Description fill="blue" />;
export const Size = () => <Description height="50" width="50" />;
export const CustomStyle = () => <Description style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Description className="custom-class" />;
