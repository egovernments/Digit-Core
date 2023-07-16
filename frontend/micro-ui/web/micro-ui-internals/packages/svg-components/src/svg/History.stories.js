import React from "react";
import { History } from "./History";

export default {
  title: "History",
  component: History,
};

export const Default = () => <History />;
export const Fill = () => <History fill="blue" />;
export const Size = () => <History height="50" width="50" />;
export const CustomStyle = () => <History style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <History className="custom-class" />;
