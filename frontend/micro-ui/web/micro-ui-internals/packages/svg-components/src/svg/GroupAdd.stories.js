import React from "react";
import { GroupAdd } from "./GroupAdd";

export default {
  title: "GroupAdd",
  component: GroupAdd,
};

export const Default = () => <GroupAdd />;
export const Fill = () => <GroupAdd fill="blue" />;
export const Size = () => <GroupAdd height="50" width="50" />;
export const CustomStyle = () => <GroupAdd style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <GroupAdd className="custom-class" />;
