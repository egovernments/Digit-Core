import React from "react";
import { ContactPage } from "./ContactPage";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ContactPage",
  component: ContactPage,
};

export const Default = () => <ContactPage />;
export const Fill = () => <ContactPage fill="blue" />;
export const Size = () => <ContactPage height="50" width="50" />;
export const CustomStyle = () => <ContactPage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ContactPage className="custom-class" />;
