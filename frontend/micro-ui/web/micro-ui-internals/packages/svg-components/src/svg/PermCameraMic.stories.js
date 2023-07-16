import React from "react";
import { PermCameraMic } from "./PermCameraMic";

export default {
  title: "PermCameraMic",
  component: PermCameraMic,
};

export const Default = () => <PermCameraMic />;
export const Fill = () => <PermCameraMic fill="blue" />;
export const Size = () => <PermCameraMic height="50" width="50" />;
export const CustomStyle = () => <PermCameraMic style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PermCameraMic className="custom-class" />;
