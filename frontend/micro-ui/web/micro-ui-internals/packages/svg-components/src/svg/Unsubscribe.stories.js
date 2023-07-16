import React from "react";
import { Unsubscribe } from "./Unsubscribe";

export default {
  title: "Unsubscribe",
  component: Unsubscribe,
};

export const Default = () => <Unsubscribe />;
export const Fill = () => <Unsubscribe fill="blue" />;
export const Size = () => <Unsubscribe height="50" width="50" />;
export const CustomStyle = () => <Unsubscribe style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Unsubscribe className="custom-class" />;
