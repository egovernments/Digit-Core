import React from "react";
import { DriveFileMoveOutline } from "./DriveFileMoveOutline";

export default {
  title: "DriveFileMoveOutline",
  component: DriveFileMoveOutline,
};

export const Default = () => <DriveFileMoveOutline />;
export const Fill = () => <DriveFileMoveOutline fill="blue" />;
export const Size = () => <DriveFileMoveOutline height="50" width="50" />;
export const CustomStyle = () => <DriveFileMoveOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DriveFileMoveOutline className="custom-class" />;
