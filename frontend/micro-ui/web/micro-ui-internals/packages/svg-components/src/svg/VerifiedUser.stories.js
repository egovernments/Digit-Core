import React from "react";
import { VerifiedUser } from "./VerifiedUser";

export default {
  title: "VerifiedUser",
  component: VerifiedUser,
};

export const Default = () => <VerifiedUser />;
export const Fill = () => <VerifiedUser fill="blue" />;
export const Size = () => <VerifiedUser height="50" width="50" />;
export const CustomStyle = () => <VerifiedUser style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <VerifiedUser className="custom-class" />;
