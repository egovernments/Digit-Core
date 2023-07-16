import React from "react";
import { PortableWifiOff } from "./PortableWifiOff";

export default {
  title: "PortableWifiOff",
  component: PortableWifiOff,
};

export const Default = () => <PortableWifiOff />;
export const Fill = () => <PortableWifiOff fill="blue" />;
export const Size = () => <PortableWifiOff height="50" width="50" />;
export const CustomStyle = () => <PortableWifiOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PortableWifiOff className="custom-class" />;
