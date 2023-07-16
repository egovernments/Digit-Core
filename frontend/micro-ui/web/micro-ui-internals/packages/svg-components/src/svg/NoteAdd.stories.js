import React from "react";
import { NoteAdd } from "./NoteAdd";

export default {
  title: "NoteAdd",
  component: NoteAdd,
};

export const Default = () => <NoteAdd />;
export const Fill = () => <NoteAdd fill="blue" />;
export const Size = () => <NoteAdd height="50" width="50" />;
export const CustomStyle = () => <NoteAdd style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NoteAdd className="custom-class" />;
