import React from "react";
import { VolunteerActivism } from "./VolunteerActivism";

export default {
  title: "VolunteerActivism",
  component: VolunteerActivism,
};

export const Default = () => <VolunteerActivism />;
export const Fill = () => <VolunteerActivism fill="blue" />;
export const Size = () => <VolunteerActivism height="50" width="50" />;
export const CustomStyle = () => <VolunteerActivism style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <VolunteerActivism className="custom-class" />;
