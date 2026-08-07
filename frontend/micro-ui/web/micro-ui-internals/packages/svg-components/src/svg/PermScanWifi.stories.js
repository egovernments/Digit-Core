import React from "react";
import { PermScanWifi } from "./PermScanWifi";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PermScanWifi",
  component: PermScanWifi,
};

export const Default = () => <PermScanWifi />;
export const Fill = () => <PermScanWifi fill="blue" />;
export const Size = () => <PermScanWifi height="50" width="50" />;
export const CustomStyle = () => <PermScanWifi style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PermScanWifi className="custom-class" />;

export const Clickable = () => <PermScanWifi onClick={()=>console.log("clicked")} />;

const Template = (args) => <PermScanWifi {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
