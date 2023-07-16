import React from "react";
import { OpenWith } from "./OpenWith";

export default {
  title: "OpenWith",
  component: OpenWith,
};

export const Default = () => <OpenWith />;
export const Fill = () => <OpenWith fill="blue" />;
export const Size = () => <OpenWith height="50" width="50" />;
export const CustomStyle = () => <OpenWith style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <OpenWith className="custom-class" />;
