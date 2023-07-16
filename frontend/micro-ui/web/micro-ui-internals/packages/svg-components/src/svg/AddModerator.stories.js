import React from "react";
import { AddModerator } from "./AddModerator";

export default {
  title: "AddModerator",
  component: AddModerator,
};

export const Default = () => <AddModerator />;
export const Fill = () => <AddModerator fill="blue" />;
export const Size = () => <AddModerator height="50" width="50" />;
export const CustomStyle = () => <AddModerator style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddModerator className="custom-class" />;
