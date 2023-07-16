import React from "react";
import { PhonelinkLock } from "./PhonelinkLock";

export default {
  title: "PhonelinkLock",
  component: PhonelinkLock,
};

export const Default = () => <PhonelinkLock />;
export const Fill = () => <PhonelinkLock fill="blue" />;
export const Size = () => <PhonelinkLock height="50" width="50" />;
export const CustomStyle = () => <PhonelinkLock style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PhonelinkLock className="custom-class" />;
