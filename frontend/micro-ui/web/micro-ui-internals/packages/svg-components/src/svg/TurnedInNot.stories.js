import React from "react";
import { TurnedInNot } from "./TurnedInNot";

export default {
  title: "TurnedInNot",
  component: TurnedInNot,
};

export const Default = () => <TurnedInNot />;
export const Fill = () => <TurnedInNot fill="blue" />;
export const Size = () => <TurnedInNot height="50" width="50" />;
export const CustomStyle = () => <TurnedInNot style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TurnedInNot className="custom-class" />;
