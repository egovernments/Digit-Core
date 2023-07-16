import React from "react";
import { AddIcCall } from "./AddIcCall";

export default {
  title: "AddIcCall",
  component: AddIcCall,
};

export const Default = () => <AddIcCall />;
export const Fill = () => <AddIcCall fill="blue" />;
export const Size = () => <AddIcCall height="50" width="50" />;
export const CustomStyle = () => <AddIcCall style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddIcCall className="custom-class" />;
