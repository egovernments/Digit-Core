import React from "react";
import { Whatshot } from "./Whatshot";

export default {
  title: "Whatshot",
  component: Whatshot,
};

export const Default = () => <Whatshot />;
export const Fill = () => <Whatshot fill="blue" />;
export const Size = () => <Whatshot height="50" width="50" />;
export const CustomStyle = () => <Whatshot style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Whatshot className="custom-class" />;
