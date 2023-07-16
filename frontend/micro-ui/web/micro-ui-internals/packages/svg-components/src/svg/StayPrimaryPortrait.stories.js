import React from "react";
import { StayPrimaryPortrait } from "./StayPrimaryPortrait";

export default {
  title: "StayPrimaryPortrait",
  component: StayPrimaryPortrait,
};

export const Default = () => <StayPrimaryPortrait />;
export const Fill = () => <StayPrimaryPortrait fill="blue" />;
export const Size = () => <StayPrimaryPortrait height="50" width="50" />;
export const CustomStyle = () => <StayPrimaryPortrait style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <StayPrimaryPortrait className="custom-class" />;
