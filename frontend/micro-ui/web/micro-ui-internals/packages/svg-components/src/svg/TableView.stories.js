import React from "react";
import { TableView } from "./TableView";

export default {
  title: "TableView",
  component: TableView,
};

export const Default = () => <TableView />;
export const Fill = () => <TableView fill="blue" />;
export const Size = () => <TableView height="50" width="50" />;
export const CustomStyle = () => <TableView style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TableView className="custom-class" />;
