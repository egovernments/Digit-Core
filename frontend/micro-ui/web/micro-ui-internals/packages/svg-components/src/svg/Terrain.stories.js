import React from "react";
import { Terrain } from "./Terrain";

export default {
  title: "Terrain",
  component: Terrain,
};

export const Default = () => <Terrain />;
export const Fill = () => <Terrain fill="blue" />;
export const Size = () => <Terrain height="50" width="50" />;
export const CustomStyle = () => <Terrain style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Terrain className="custom-class" />;
