import React from "react";
import { WifiProtectedSetup } from "./WifiProtectedSetup";

export default {
  title: "WifiProtectedSetup",
  component: WifiProtectedSetup,
};

export const Default = () => <WifiProtectedSetup />;
export const Fill = () => <WifiProtectedSetup fill="blue" />;
export const Size = () => <WifiProtectedSetup height="50" width="50" />;
export const CustomStyle = () => <WifiProtectedSetup style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WifiProtectedSetup className="custom-class" />;
