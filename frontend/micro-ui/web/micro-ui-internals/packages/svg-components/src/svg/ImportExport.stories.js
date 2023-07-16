import React from "react";
import { ImportExport } from "./ImportExport";

export default {
  title: "ImportExport",
  component: ImportExport,
};

export const Default = () => <ImportExport />;
export const Fill = () => <ImportExport fill="blue" />;
export const Size = () => <ImportExport height="50" width="50" />;
export const CustomStyle = () => <ImportExport style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ImportExport className="custom-class" />;
