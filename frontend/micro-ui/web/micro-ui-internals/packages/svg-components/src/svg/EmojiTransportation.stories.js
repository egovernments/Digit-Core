import React from "react";
import { EmojiTransportation } from "./EmojiTransportation";

export default {
  title: "EmojiTransportation",
  component: EmojiTransportation,
};

export const Default = () => <EmojiTransportation />;
export const Fill = () => <EmojiTransportation fill="blue" />;
export const Size = () => <EmojiTransportation height="50" width="50" />;
export const CustomStyle = () => <EmojiTransportation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EmojiTransportation className="custom-class" />;
