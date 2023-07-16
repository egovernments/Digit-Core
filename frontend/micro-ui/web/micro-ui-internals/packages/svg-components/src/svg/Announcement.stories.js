import React from "react";
import { Announcement } from "./Announcement";

export default {
  title: "Announcement",
  component: Announcement,
};

export const Default = () => <Announcement />;
export const Fill = () => <Announcement fill="blue" />;
export const Size = () => <Announcement height="50" width="50" />;
export const CustomStyle = () => <Announcement style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Announcement className="custom-class" />;
