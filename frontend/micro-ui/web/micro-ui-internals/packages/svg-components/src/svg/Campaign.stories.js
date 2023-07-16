import React from "react";
import { Campaign } from "./Campaign";

export default {
  title: "Campaign",
  component: Campaign,
};

export const Default = () => <Campaign />;
export const Fill = () => <Campaign fill="blue" />;
export const Size = () => <Campaign height="50" width="50" />;
export const CustomStyle = () => <Campaign style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Campaign className="custom-class" />;
