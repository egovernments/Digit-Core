import React from "react";
import { SportsKabaddi } from "./SportsKabaddi";

export default {
  title: "SportsKabaddi",
  component: SportsKabaddi,
};

export const Default = () => <SportsKabaddi />;
export const Fill = () => <SportsKabaddi fill="blue" />;
export const Size = () => <SportsKabaddi height="50" width="50" />;
export const CustomStyle = () => <SportsKabaddi style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsKabaddi className="custom-class" />;
