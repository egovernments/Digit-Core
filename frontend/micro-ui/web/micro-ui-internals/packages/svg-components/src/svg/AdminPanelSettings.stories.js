import React from "react";
import { AdminPanelSettings } from "./AdminPanelSettings";

export default {
  title: "AdminPanelSettings",
  component: AdminPanelSettings,
};

export const Default = () => <AdminPanelSettings />;
export const Fill = () => <AdminPanelSettings fill="blue" />;
export const Size = () => <AdminPanelSettings height="50" width="50" />;
export const CustomStyle = () => <AdminPanelSettings style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AdminPanelSettings className="custom-class" />;
