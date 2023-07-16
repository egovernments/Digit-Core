import React from "react";
import { GetApp } from "./GetApp";

export default {
  title: "GetApp",
  component: GetApp,
};

export const Default = () => <GetApp />;
export const Fill = () => <GetApp fill="blue" />;
export const Size = () => <GetApp height="50" width="50" />;
export const CustomStyle = () => <GetApp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <GetApp className="custom-class" />;
