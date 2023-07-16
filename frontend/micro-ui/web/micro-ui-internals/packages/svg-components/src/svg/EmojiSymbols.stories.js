import React from "react";
import { EmojiSymbols } from "./EmojiSymbols";

export default {
  title: "EmojiSymbols",
  component: EmojiSymbols,
};

export const Default = () => <EmojiSymbols />;
export const Fill = () => <EmojiSymbols fill="blue" />;
export const Size = () => <EmojiSymbols height="50" width="50" />;
export const CustomStyle = () => <EmojiSymbols style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EmojiSymbols className="custom-class" />;
