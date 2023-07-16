import React from "react";
import { StayPrimaryLandscape } from "./StayPrimaryLandscape";

export default {
  title: "StayPrimaryLandscape",
  component: StayPrimaryLandscape,
};

export const Default = () => <StayPrimaryLandscape />;
export const Fill = () => <StayPrimaryLandscape fill="blue" />;
export const Size = () => <StayPrimaryLandscape height="50" width="50" />;
export const CustomStyle = () => <StayPrimaryLandscape style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <StayPrimaryLandscape className="custom-class" />;
