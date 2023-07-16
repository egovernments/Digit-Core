import React from "react";
import { AddShoppingCart } from "./AddShoppingCart";

export default {
  title: "AddShoppingCart",
  component: AddShoppingCart,
};

export const Default = () => <AddShoppingCart />;
export const Fill = () => <AddShoppingCart fill="blue" />;
export const Size = () => <AddShoppingCart height="50" width="50" />;
export const CustomStyle = () => <AddShoppingCart style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddShoppingCart className="custom-class" />;
