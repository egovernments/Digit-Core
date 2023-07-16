import React from "react";
import { EmojiObjects } from "./EmojiObjects";

export default {
  title: "EmojiObjects",
  component: EmojiObjects,
};

export const Default = () => <EmojiObjects />;
export const Fill = () => <EmojiObjects fill="blue" />;
export const Size = () => <EmojiObjects height="50" width="50" />;
export const CustomStyle = () => <EmojiObjects style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EmojiObjects className="custom-class" />;
