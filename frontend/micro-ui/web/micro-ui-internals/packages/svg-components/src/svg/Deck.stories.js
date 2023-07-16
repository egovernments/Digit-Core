import React from "react";
import { Deck } from "./Deck";

export default {
  title: "Deck",
  component: Deck,
};

export const Default = () => <Deck />;
export const Fill = () => <Deck fill="blue" />;
export const Size = () => <Deck height="50" width="50" />;
export const CustomStyle = () => <Deck style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Deck className="custom-class" />;
