import React from "react";
import { HomeWork } from "./HomeWork";

export default {
  title: "HomeWork",
  component: HomeWork,
};

export const Default = () => <HomeWork />;
export const Fill = () => <HomeWork fill="blue" />;
export const Size = () => <HomeWork height="50" width="50" />;
export const CustomStyle = () => <HomeWork style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HomeWork className="custom-class" />;
