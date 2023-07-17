import React from "react";
import { PermDeviceInformation } from "./PermDeviceInformation";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PermDeviceInformation",
  component: PermDeviceInformation,
};

export const Default = () => <PermDeviceInformation />;
export const Fill = () => <PermDeviceInformation fill="blue" />;
export const Size = () => <PermDeviceInformation height="50" width="50" />;
export const CustomStyle = () => <PermDeviceInformation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PermDeviceInformation className="custom-class" />;

export const Clickable = () => <PermDeviceInformation onClick={()=>console.log("clicked")} />;

const Template = (args) => <PermDeviceInformation {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
