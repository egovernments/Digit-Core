import React from "react";
import { DoneOutline } from "./DoneOutline";

export default {
  title: "DoneOutline",
  component: DoneOutline,
};

export const Default = () => <DoneOutline />;
export const Fill = () => <DoneOutline fill="blue" />;
export const Size = () => <DoneOutline height="50" width="50" />;
export const CustomStyle = () => <DoneOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DoneOutline className="custom-class" />;
