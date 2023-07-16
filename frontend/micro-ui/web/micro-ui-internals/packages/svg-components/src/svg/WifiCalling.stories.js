import React from "react";
import { WifiCalling } from "./WifiCalling";

export default {
  title: "WifiCalling",
  component: WifiCalling,
};

export const Default = () => <WifiCalling />;
export const Fill = () => <WifiCalling fill="blue" />;
export const Size = () => <WifiCalling height="50" width="50" />;
export const CustomStyle = () => <WifiCalling style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WifiCalling className="custom-class" />;
