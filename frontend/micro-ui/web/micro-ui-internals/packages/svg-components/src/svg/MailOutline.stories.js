import React from "react";
import { MailOutline } from "./MailOutline";

export default {
  title: "MailOutline",
  component: MailOutline,
};

export const Default = () => <MailOutline />;
export const Fill = () => <MailOutline fill="blue" />;
export const Size = () => <MailOutline height="50" width="50" />;
export const CustomStyle = () => <MailOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MailOutline className="custom-class" />;
