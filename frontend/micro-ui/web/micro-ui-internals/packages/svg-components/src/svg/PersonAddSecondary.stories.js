import React from "react";
import { PersonAddSecondary } from "./PersonAddSecondary";

export default {
  title: "PersonAddSecondary",
  component: PersonAddSecondary,
};

export const Default = () => <PersonAddSecondary />;
export const Fill = () => <PersonAddSecondary fill="blue" />;
export const Size = () => <PersonAddSecondary height="50" width="50" />;
export const CustomStyle = () => <PersonAddSecondary style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PersonAddSecondary className="custom-class" />;
