import React from "react";
import { TorchNoun } from "./TorchNoun";

export default {
  title: "TorchNoun",
  component: TorchNoun,
};

export const Default = () => <TorchNoun />;
export const Fill = () => <TorchNoun fill="blue" />;
export const Size = () => <TorchNoun height="50" width="50" />;
export const CustomStyle = () => <TorchNoun style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TorchNoun className="custom-class" />;
