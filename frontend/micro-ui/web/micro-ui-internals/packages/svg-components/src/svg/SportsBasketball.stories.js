import React from "react";
import { SportsBasketball } from "./SportsBasketball";

export default {
  title: "SportsBasketball",
  component: SportsBasketball,
};

export const Default = () => <SportsBasketball />;
export const Fill = () => <SportsBasketball fill="blue" />;
export const Size = () => <SportsBasketball height="50" width="50" />;
export const CustomStyle = () => <SportsBasketball style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsBasketball className="custom-class" />;
