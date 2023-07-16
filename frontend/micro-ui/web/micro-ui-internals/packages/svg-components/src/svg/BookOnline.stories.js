import React from "react";
import { BookOnline } from "./BookOnline";

export default {
  title: "BookOnline",
  component: BookOnline,
};

export const Default = () => <BookOnline />;
export const Fill = () => <BookOnline fill="blue" />;
export const Size = () => <BookOnline height="50" width="50" />;
export const CustomStyle = () => <BookOnline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BookOnline className="custom-class" />;
