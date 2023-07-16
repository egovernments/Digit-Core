import React from "react";
import { TripOrigin } from "./TripOrigin";

export default {
  title: "TripOrigin",
  component: TripOrigin,
};

export const Default = () => <TripOrigin />;
export const Fill = () => <TripOrigin fill="blue" />;
export const Size = () => <TripOrigin height="50" width="50" />;
export const CustomStyle = () => <TripOrigin style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TripOrigin className="custom-class" />;
