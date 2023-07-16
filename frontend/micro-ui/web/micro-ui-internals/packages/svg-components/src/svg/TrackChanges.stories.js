import React from "react";
import { TrackChanges } from "./TrackChanges";

export default {
  title: "TrackChanges",
  component: TrackChanges,
};

export const Default = () => <TrackChanges />;
export const Fill = () => <TrackChanges fill="blue" />;
export const Size = () => <TrackChanges height="50" width="50" />;
export const CustomStyle = () => <TrackChanges style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TrackChanges className="custom-class" />;
