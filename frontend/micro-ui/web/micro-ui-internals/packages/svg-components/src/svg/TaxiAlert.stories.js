import React from "react";
import { TaxiAlert } from "./TaxiAlert";

export default {
  title: "TaxiAlert",
  component: TaxiAlert,
};

export const Default = () => <TaxiAlert />;
export const Fill = () => <TaxiAlert fill="blue" />;
export const Size = () => <TaxiAlert height="50" width="50" />;
export const CustomStyle = () => <TaxiAlert style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TaxiAlert className="custom-class" />;
