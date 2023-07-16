import React from "react";
import { Compress } from "./Compress";

export default {
  title: "Compress",
  component: Compress,
};

export const Default = () => <Compress />;
export const Fill = () => <Compress fill="blue" />;
export const Size = () => <Compress height="50" width="50" />;
export const CustomStyle = () => <Compress style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Compress className="custom-class" />;
