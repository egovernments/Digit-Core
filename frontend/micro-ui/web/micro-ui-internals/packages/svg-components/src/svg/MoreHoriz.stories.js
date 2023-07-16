import React from "react";
import { MoreHoriz } from "./MoreHoriz";

export default {
  title: "MoreHoriz",
  component: MoreHoriz,
};

export const Default = () => <MoreHoriz />;
export const Fill = () => <MoreHoriz fill="blue" />;
export const Size = () => <MoreHoriz height="50" width="50" />;
export const CustomStyle = () => <MoreHoriz style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MoreHoriz className="custom-class" />;
