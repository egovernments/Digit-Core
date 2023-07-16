import React from "react";
import { WrongLocation } from "./WrongLocation";

export default {
  title: "WrongLocation",
  component: WrongLocation,
};

export const Default = () => <WrongLocation />;
export const Fill = () => <WrongLocation fill="blue" />;
export const Size = () => <WrongLocation height="50" width="50" />;
export const CustomStyle = () => <WrongLocation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WrongLocation className="custom-class" />;
