import React from "react";
import { ImportantDevices } from "./ImportantDevices";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ImportantDevices",
  component: ImportantDevices,
};

export const Default = () => <ImportantDevices />;
export const Fill = () => <ImportantDevices fill="blue" />;
export const Size = () => <ImportantDevices height="50" width="50" />;
export const CustomStyle = () => <ImportantDevices style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ImportantDevices className="custom-class" />;

export const Clickable = () => <ImportantDevices onClick={()=>console.log("clicked")} />;

const Template = (args) => <ImportantDevices {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
