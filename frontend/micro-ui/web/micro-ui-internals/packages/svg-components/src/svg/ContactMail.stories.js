import React from "react";
import { ContactMail } from "./ContactMail";

export default {
  title: "ContactMail",
  component: ContactMail,
};

export const Default = () => <ContactMail />;
export const Fill = () => <ContactMail fill="blue" />;
export const Size = () => <ContactMail height="50" width="50" />;
export const CustomStyle = () => <ContactMail style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ContactMail className="custom-class" />;
