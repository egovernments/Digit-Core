import React from "react";
import { Backup } from "./Backup";

export default {
  title: "Backup",
  component: Backup,
};

export const Default = () => <Backup />;
export const Fill = () => <Backup fill="blue" />;
export const Size = () => <Backup height="50" width="50" />;
export const CustomStyle = () => <Backup style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Backup className="custom-class" />;
