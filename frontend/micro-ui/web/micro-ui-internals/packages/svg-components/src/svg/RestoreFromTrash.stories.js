import React from "react";
import { RestoreFromTrash } from "./RestoreFromTrash";

export default {
  title: "RestoreFromTrash",
  component: RestoreFromTrash,
};

export const Default = () => <RestoreFromTrash />;
export const Fill = () => <RestoreFromTrash fill="blue" />;
export const Size = () => <RestoreFromTrash height="50" width="50" />;
export const CustomStyle = () => <RestoreFromTrash style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RestoreFromTrash className="custom-class" />;
