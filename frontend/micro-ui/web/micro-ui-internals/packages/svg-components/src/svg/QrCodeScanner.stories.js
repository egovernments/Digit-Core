import React from "react";
import { QrCodeScanner } from "./QrCodeScanner";

export default {
  title: "QrCodeScanner",
  component: QrCodeScanner,
};

export const Default = () => <QrCodeScanner />;
export const Fill = () => <QrCodeScanner fill="blue" />;
export const Size = () => <QrCodeScanner height="50" width="50" />;
export const CustomStyle = () => <QrCodeScanner style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <QrCodeScanner className="custom-class" />;
