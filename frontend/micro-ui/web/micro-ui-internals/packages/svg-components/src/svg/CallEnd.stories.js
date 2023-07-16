import React from "react";
import { CallEnd } from "./CallEnd";

export default {
  title: "CallEnd",
  component: CallEnd,
};

export const Default = () => <CallEnd />;
export const Fill = () => <CallEnd fill="blue" />;
export const Size = () => <CallEnd height="50" width="50" />;
export const CustomStyle = () => <CallEnd style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CallEnd className="custom-class" />;
