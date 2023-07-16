import React from "react";
import { PedalBike } from "./PedalBike";

export default {
  title: "PedalBike",
  component: PedalBike,
};

export const Default = () => <PedalBike />;
export const Fill = () => <PedalBike fill="blue" />;
export const Size = () => <PedalBike height="50" width="50" />;
export const CustomStyle = () => <PedalBike style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PedalBike className="custom-class" />;
