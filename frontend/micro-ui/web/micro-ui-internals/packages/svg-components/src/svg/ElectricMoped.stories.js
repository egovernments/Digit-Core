import React from "react";
import { ElectricMoped } from "./ElectricMoped";

export default {
  title: "ElectricMoped",
  component: ElectricMoped,
};

export const Default = () => <ElectricMoped />;
export const Fill = () => <ElectricMoped fill="blue" />;
export const Size = () => <ElectricMoped height="50" width="50" />;
export const CustomStyle = () => <ElectricMoped style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ElectricMoped className="custom-class" />;
