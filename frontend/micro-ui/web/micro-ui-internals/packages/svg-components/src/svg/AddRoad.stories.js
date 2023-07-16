import React from "react";
import { AddRoad } from "./AddRoad";

export default {
  title: "AddRoad",
  component: AddRoad,
};

export const Default = () => <AddRoad />;
export const Fill = () => <AddRoad fill="blue" />;
export const Size = () => <AddRoad height="50" width="50" />;
export const CustomStyle = () => <AddRoad style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddRoad className="custom-class" />;
