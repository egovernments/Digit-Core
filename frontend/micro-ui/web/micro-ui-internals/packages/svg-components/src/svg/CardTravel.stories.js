import React from "react";
import { CardTravel } from "./CardTravel";

export default {
  title: "CardTravel",
  component: CardTravel,
};

export const Default = () => <CardTravel />;
export const Fill = () => <CardTravel fill="blue" />;
export const Size = () => <CardTravel height="50" width="50" />;
export const CustomStyle = () => <CardTravel style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CardTravel className="custom-class" />;
