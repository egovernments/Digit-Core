import React from "react";
import { AddBusiness } from "./AddBusiness";

export default {
  title: "AddBusiness",
  component: AddBusiness,
};

export const Default = () => <AddBusiness />;
export const Fill = () => <AddBusiness fill="blue" />;
export const Size = () => <AddBusiness height="50" width="50" />;
export const CustomStyle = () => <AddBusiness style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddBusiness className="custom-class" />;
