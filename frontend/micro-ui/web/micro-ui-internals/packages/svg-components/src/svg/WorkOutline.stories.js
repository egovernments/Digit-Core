import React from "react";
import { WorkOutline } from "./WorkOutline";

export default {
  title: "WorkOutline",
  component: WorkOutline,
};

export const Default = () => <WorkOutline />;
export const Fill = () => <WorkOutline fill="blue" />;
export const Size = () => <WorkOutline height="50" width="50" />;
export const CustomStyle = () => <WorkOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WorkOutline className="custom-class" />;
