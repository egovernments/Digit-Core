import React from "react";
import { AddLocation } from "./AddLocation";

export default {
  title: "AddLocation",
  component: AddLocation,
};

export const Default = () => <AddLocation />;
export const Fill = () => <AddLocation fill="blue" />;
export const Size = () => <AddLocation height="50" width="50" />;
export const CustomStyle = () => <AddLocation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddLocation className="custom-class" />;
