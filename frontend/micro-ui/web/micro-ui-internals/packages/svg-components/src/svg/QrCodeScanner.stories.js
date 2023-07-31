import React from "react";
import { QrCodeScanner } from "./QrCodeScanner";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "QrCodeScanner",
  component: QrCodeScanner,
};

export const Default = () => <QrCodeScanner />;
export const Fill = () => <QrCodeScanner fill="blue" />;
export const Size = () => <QrCodeScanner height="50" width="50" />;
export const CustomStyle = () => <QrCodeScanner style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <QrCodeScanner className="custom-class" />;

export const Clickable = () => <QrCodeScanner onClick={()=>console.log("clicked")} />;

const Template = (args) => <QrCodeScanner {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
