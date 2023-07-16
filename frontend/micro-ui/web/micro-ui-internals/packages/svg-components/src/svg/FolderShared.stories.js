import React from "react";
import { FolderShared } from "./FolderShared";

export default {
  title: "FolderShared",
  component: FolderShared,
};

export const Default = () => <FolderShared />;
export const Fill = () => <FolderShared fill="blue" />;
export const Size = () => <FolderShared height="50" width="50" />;
export const CustomStyle = () => <FolderShared style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FolderShared className="custom-class" />;
