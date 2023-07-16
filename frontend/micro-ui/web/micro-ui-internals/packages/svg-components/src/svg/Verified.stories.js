import React from "react";
import { Verified } from "./Verified";

export default {
  title: "Verified",
  component: Verified,
};

export const Default = () => <Verified />;
export const Fill = () => <Verified fill="blue" />;
export const Size = () => <Verified height="50" width="50" />;
export const CustomStyle = () => <Verified style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Verified className="custom-class" />;
