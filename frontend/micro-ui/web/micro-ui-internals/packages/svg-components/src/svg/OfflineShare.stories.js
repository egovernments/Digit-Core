import React from "react";
import { OfflineShare } from "./OfflineShare";

export default {
  title: "OfflineShare",
  component: OfflineShare,
};

export const Default = () => <OfflineShare />;
export const Fill = () => <OfflineShare fill="blue" />;
export const Size = () => <OfflineShare height="50" width="50" />;
export const CustomStyle = () => <OfflineShare style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <OfflineShare className="custom-class" />;
