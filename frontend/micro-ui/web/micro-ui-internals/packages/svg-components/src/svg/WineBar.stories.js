import React from "react";
import { WineBar } from "./WineBar";

export default {
  title: "WineBar",
  component: WineBar,
};

export const Default = () => <WineBar />;
export const Fill = () => <WineBar fill="blue" />;
export const Size = () => <WineBar height="50" width="50" />;
export const CustomStyle = () => <WineBar style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WineBar className="custom-class" />;
