import React from "react";
import { WorkspacesFilled } from "./WorkspacesFilled";

export default {
  title: "WorkspacesFilled",
  component: WorkspacesFilled,
};

export const Default = () => <WorkspacesFilled />;
export const Fill = () => <WorkspacesFilled fill="blue" />;
export const Size = () => <WorkspacesFilled height="50" width="50" />;
export const CustomStyle = () => <WorkspacesFilled style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WorkspacesFilled className="custom-class" />;
