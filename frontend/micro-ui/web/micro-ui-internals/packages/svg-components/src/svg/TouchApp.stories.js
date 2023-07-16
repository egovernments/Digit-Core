import React from "react";
import { TouchApp } from "./TouchApp";

export default {
  title: "TouchApp",
  component: TouchApp,
};

export const Default = () => <TouchApp />;
export const Fill = () => <TouchApp fill="blue" />;
export const Size = () => <TouchApp height="50" width="50" />;
export const CustomStyle = () => <TouchApp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TouchApp className="custom-class" />;
