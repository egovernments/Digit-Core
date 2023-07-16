import React from "react";
import { Anchor } from "./Anchor";

export default {
  title: "Anchor",
  component: Anchor,
};

export const Default = () => <Anchor />;
export const Fill = () => <Anchor fill="blue" />;
export const Size = () => <Anchor height="50" width="50" />;
export const CustomStyle = () => <Anchor style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Anchor className="custom-class" />;
