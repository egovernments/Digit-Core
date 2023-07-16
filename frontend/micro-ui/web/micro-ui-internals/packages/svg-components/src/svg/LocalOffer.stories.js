import React from "react";
import { LocalOffer } from "./LocalOffer";

export default {
  title: "LocalOffer",
  component: LocalOffer,
};

export const Default = () => <LocalOffer />;
export const Fill = () => <LocalOffer fill="blue" />;
export const Size = () => <LocalOffer height="50" width="50" />;
export const CustomStyle = () => <LocalOffer style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalOffer className="custom-class" />;
