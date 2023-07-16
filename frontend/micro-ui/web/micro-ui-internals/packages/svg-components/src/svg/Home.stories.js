import React from "react";
import { Home } from "./Home";

export default {
  title: "Home",
  component: Home,
};

export const Default = () => <Home />;
export const Fill = () => <Home fill="blue" />;
export const Size = () => <Home height="50" width="50" />;
export const CustomStyle = () => <Home style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Home className="custom-class" />;
