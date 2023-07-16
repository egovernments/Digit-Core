import React from "react";
import { Bookmarks } from "./Bookmarks";

export default {
  title: "Bookmarks",
  component: Bookmarks,
};

export const Default = () => <Bookmarks />;
export const Fill = () => <Bookmarks fill="blue" />;
export const Size = () => <Bookmarks height="50" width="50" />;
export const CustomStyle = () => <Bookmarks style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Bookmarks className="custom-class" />;
