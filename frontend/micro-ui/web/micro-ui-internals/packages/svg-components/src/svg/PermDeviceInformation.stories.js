import React from "react";
import { PermDeviceInformation } from "./PermDeviceInformation";

export default {
  title: "PermDeviceInformation",
  component: PermDeviceInformation,
};

export const Default = () => <PermDeviceInformation />;
export const Fill = () => <PermDeviceInformation fill="blue" />;
export const Size = () => <PermDeviceInformation height="50" width="50" />;
export const CustomStyle = () => <PermDeviceInformation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PermDeviceInformation className="custom-class" />;
