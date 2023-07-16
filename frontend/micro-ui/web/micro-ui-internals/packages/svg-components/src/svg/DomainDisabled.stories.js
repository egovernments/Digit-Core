import React from "react";
import { DomainDisabled } from "./DomainDisabled";

export default {
  title: "DomainDisabled",
  component: DomainDisabled,
};

export const Default = () => <DomainDisabled />;
export const Fill = () => <DomainDisabled fill="blue" />;
export const Size = () => <DomainDisabled height="50" width="50" />;
export const CustomStyle = () => <DomainDisabled style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DomainDisabled className="custom-class" />;
