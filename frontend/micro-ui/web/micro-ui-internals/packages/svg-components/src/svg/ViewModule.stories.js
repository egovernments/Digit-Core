import React from "react";
import { ViewModule } from "./ViewModule";

export default {
  title: "ViewModule",
  component: ViewModule,
};

export const Default = () => <ViewModule />;
export const Fill = () => <ViewModule fill="blue" />;
export const Size = () => <ViewModule height="50" width="50" />;
export const CustomStyle = () => <ViewModule style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewModule className="custom-class" />;
