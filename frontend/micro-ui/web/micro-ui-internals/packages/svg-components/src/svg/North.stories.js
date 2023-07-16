import React from "react";
import { North } from "./North";

export default {
  title: "North",
  component: North,
};

export const Default = () => <North />;
export const Fill = () => <North fill="blue" />;
export const Size = () => <North height="50" width="50" />;
export const CustomStyle = () => <North style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <North className="custom-class" />;
