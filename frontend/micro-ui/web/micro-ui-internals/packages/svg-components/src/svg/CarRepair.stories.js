import React from "react";
import { CarRepair } from "./CarRepair";

export default {
  title: "CarRepair",
  component: CarRepair,
};

export const Default = () => <CarRepair />;
export const Fill = () => <CarRepair fill="blue" />;
export const Size = () => <CarRepair height="50" width="50" />;
export const CustomStyle = () => <CarRepair style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CarRepair className="custom-class" />;
