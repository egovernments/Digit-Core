import React from "react";
import { NoLuggage } from "./NoLuggage";

export default {
  title: "NoLuggage",
  component: NoLuggage,
};

export const Default = () => <NoLuggage />;
export const Fill = () => <NoLuggage fill="blue" />;
export const Size = () => <NoLuggage height="50" width="50" />;
export const CustomStyle = () => <NoLuggage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NoLuggage className="custom-class" />;
