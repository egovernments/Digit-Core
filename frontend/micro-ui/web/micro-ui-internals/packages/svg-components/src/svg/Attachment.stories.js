import React from "react";
import { Attachment } from "./Attachment";

export default {
  title: "Attachment",
  component: Attachment,
};

export const Default = () => <Attachment />;
export const Fill = () => <Attachment fill="blue" />;
export const Size = () => <Attachment height="50" width="50" />;
export const CustomStyle = () => <Attachment style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Attachment className="custom-class" />;
