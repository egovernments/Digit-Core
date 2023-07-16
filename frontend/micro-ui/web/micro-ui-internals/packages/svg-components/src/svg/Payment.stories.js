import React from "react";
import { Payment } from "./Payment";

export default {
  title: "Payment",
  component: Payment,
};

export const Default = () => <Payment />;
export const Fill = () => <Payment fill="blue" />;
export const Size = () => <Payment height="50" width="50" />;
export const CustomStyle = () => <Payment style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Payment className="custom-class" />;
