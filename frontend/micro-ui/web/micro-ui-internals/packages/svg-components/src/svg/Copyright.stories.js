import React from "react";
import { Copyright } from "./Copyright";

export default {
  title: "Copyright",
  component: Copyright,
};

export const Default = () => <Copyright />;
export const Fill = () => <Copyright fill="blue" />;
export const Size = () => <Copyright height="50" width="50" />;
export const CustomStyle = () => <Copyright style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Copyright className="custom-class" />;
