import React from "react";
import { Folder } from "./Folder";

export default {
  title: "Folder",
  component: Folder,
};

export const Default = () => <Folder />;
export const Fill = () => <Folder fill="blue" />;
export const Size = () => <Folder height="50" width="50" />;
export const CustomStyle = () => <Folder style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Folder className="custom-class" />;
