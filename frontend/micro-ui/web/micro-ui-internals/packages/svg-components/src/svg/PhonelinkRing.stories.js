import React from "react";
import { PhonelinkRing } from "./PhonelinkRing";

export default {
  title: "PhonelinkRing",
  component: PhonelinkRing,
};

export const Default = () => <PhonelinkRing />;
export const Fill = () => <PhonelinkRing fill="blue" />;
export const Size = () => <PhonelinkRing height="50" width="50" />;
export const CustomStyle = () => <PhonelinkRing style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PhonelinkRing className="custom-class" />;
