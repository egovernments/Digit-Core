import React from "react";
import { PortableWifiOff } from "./PortableWifiOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PortableWifiOff",
  component: PortableWifiOff,
};

export const Default = () => <PortableWifiOff />;
export const Fill = () => <PortableWifiOff fill="blue" />;
export const Size = () => <PortableWifiOff height="50" width="50" />;
export const CustomStyle = () => <PortableWifiOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PortableWifiOff className="custom-class" />;

export const Clickable = () => <PortableWifiOff onClick={()=>console.log("clicked")} />;

const Template = (args) => <PortableWifiOff {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
