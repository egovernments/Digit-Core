import React from "react";
import { ImportContacts } from "./ImportContacts";

export default {
  title: "ImportContacts",
  component: ImportContacts,
};

export const Default = () => <ImportContacts />;
export const Fill = () => <ImportContacts fill="blue" />;
export const Size = () => <ImportContacts height="50" width="50" />;
export const CustomStyle = () => <ImportContacts style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ImportContacts className="custom-class" />;
