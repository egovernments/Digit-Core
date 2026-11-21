import React from "react";
import { QrCode } from "./QrCode";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "QrCode",
  component: QrCode,
};

export const Default = () => <QrCode />;
export const Fill = () => <QrCode fill="blue" />;
export const Size = () => <QrCode height="50" width="50" />;
export const CustomStyle = () => <QrCode style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <QrCode className="custom-class" />;

export const Clickable = () => <QrCode onClick={()=>console.log("clicked")} />;

const Template = (args) => <QrCode {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
