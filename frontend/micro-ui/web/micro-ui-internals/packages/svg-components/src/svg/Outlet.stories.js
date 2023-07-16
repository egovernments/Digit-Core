import React from "react";
import { Outlet } from "./Outlet";

export default {
  title: "Outlet",
  component: Outlet,
};

export const Default = () => <Outlet />;
export const Fill = () => <Outlet fill="blue" />;
export const Size = () => <Outlet height="50" width="50" />;
export const CustomStyle = () => <Outlet style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Outlet className="custom-class" />;
