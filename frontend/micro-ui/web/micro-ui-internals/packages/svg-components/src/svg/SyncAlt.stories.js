import React from "react";
import { SyncAlt } from "./SyncAlt";

export default {
  title: "SyncAlt",
  component: SyncAlt,
};

export const Default = () => <SyncAlt />;
export const Fill = () => <SyncAlt fill="blue" />;
export const Size = () => <SyncAlt height="50" width="50" />;
export const CustomStyle = () => <SyncAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SyncAlt className="custom-class" />;
