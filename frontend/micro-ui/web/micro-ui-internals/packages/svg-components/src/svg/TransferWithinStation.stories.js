import React from "react";
import { TransferWithinStation } from "./TransferWithinStation";

export default {
  title: "TransferWithinStation",
  component: TransferWithinStation,
};

export const Default = () => <TransferWithinStation />;
export const Fill = () => <TransferWithinStation fill="blue" />;
export const Size = () => <TransferWithinStation height="50" width="50" />;
export const CustomStyle = () => <TransferWithinStation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TransferWithinStation className="custom-class" />;
