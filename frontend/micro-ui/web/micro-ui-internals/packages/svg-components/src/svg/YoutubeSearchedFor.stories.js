import React from "react";
import { YoutubeSearchedFor } from "./YoutubeSearchedFor";

export default {
  title: "YoutubeSearchedFor",
  component: YoutubeSearchedFor,
};

export const Default = () => <YoutubeSearchedFor />;
export const Fill = () => <YoutubeSearchedFor fill="blue" />;
export const Size = () => <YoutubeSearchedFor height="50" width="50" />;
export const CustomStyle = () => <YoutubeSearchedFor style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <YoutubeSearchedFor className="custom-class" />;
