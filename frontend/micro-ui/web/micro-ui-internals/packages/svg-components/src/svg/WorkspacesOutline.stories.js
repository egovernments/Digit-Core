import React from "react";
import { WorkspacesOutline } from "./WorkspacesOutline";

export default {
  title: "WorkspacesOutline",
  component: WorkspacesOutline,
};

export const Default = () => <WorkspacesOutline />;
export const Fill = () => <WorkspacesOutline fill="blue" />;
export const Size = () => <WorkspacesOutline height="50" width="50" />;
export const CustomStyle = () => <WorkspacesOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WorkspacesOutline className="custom-class" />;
