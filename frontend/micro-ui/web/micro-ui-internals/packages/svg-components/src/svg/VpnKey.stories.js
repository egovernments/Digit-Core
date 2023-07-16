import React from "react";
import { VpnKey } from "./VpnKey";

export default {
  title: "VpnKey",
  component: VpnKey,
};

export const Default = () => <VpnKey />;
export const Fill = () => <VpnKey fill="blue" />;
export const Size = () => <VpnKey height="50" width="50" />;
export const CustomStyle = () => <VpnKey style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <VpnKey className="custom-class" />;
