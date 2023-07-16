import React from "react";
import { TurnedIn } from "./TurnedIn";

export default {
  title: "TurnedIn",
  component: TurnedIn,
};

export const Default = () => <TurnedIn />;
export const Fill = () => <TurnedIn fill="blue" />;
export const Size = () => <TurnedIn height="50" width="50" />;
export const CustomStyle = () => <TurnedIn style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TurnedIn className="custom-class" />;
