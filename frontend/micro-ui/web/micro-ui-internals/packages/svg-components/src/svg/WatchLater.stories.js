import React from "react";
import { WatchLater } from "./WatchLater";

export default {
  title: "WatchLater",
  component: WatchLater,
};

export const Default = () => <WatchLater />;
export const Fill = () => <WatchLater fill="blue" />;
export const Size = () => <WatchLater height="50" width="50" />;
export const CustomStyle = () => <WatchLater style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WatchLater className="custom-class" />;
