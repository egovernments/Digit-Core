import React from "react";
import { BikeScooter } from "./BikeScooter";

export default {
  title: "BikeScooter",
  component: BikeScooter,
};

export const Default = () => <BikeScooter />;
export const Fill = () => <BikeScooter fill="blue" />;
export const Size = () => <BikeScooter height="50" width="50" />;
export const CustomStyle = () => <BikeScooter style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BikeScooter className="custom-class" />;
