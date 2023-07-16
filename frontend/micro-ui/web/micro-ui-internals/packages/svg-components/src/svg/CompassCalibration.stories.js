import React from "react";
import { CompassCalibration } from "./CompassCalibration";

export default {
  title: "CompassCalibration",
  component: CompassCalibration,
};

export const Default = () => <CompassCalibration />;
export const Fill = () => <CompassCalibration fill="blue" />;
export const Size = () => <CompassCalibration height="50" width="50" />;
export const CustomStyle = () => <CompassCalibration style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CompassCalibration className="custom-class" />;
