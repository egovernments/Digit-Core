import React from "react";
import { Whatsapp } from "./Whatsapp";

export default {
  title: "Whatsapp",
  component: Whatsapp,
};

export const Default = () => <Whatsapp />;
export const Fill = () => <Whatsapp fill="blue" />;
export const Size = () => <Whatsapp height="50" width="50" />;
export const CustomStyle = () => <Whatsapp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Whatsapp className="custom-class" />;
