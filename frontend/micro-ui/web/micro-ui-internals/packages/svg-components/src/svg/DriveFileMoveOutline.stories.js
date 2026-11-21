import React from "react";
import { DriveFileMoveOutline } from "./DriveFileMoveOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DriveFileMoveOutline",
  component: DriveFileMoveOutline,
};

export const Default = () => <DriveFileMoveOutline />;
export const Fill = () => <DriveFileMoveOutline fill="blue" />;
export const Size = () => <DriveFileMoveOutline height="50" width="50" />;
export const CustomStyle = () => <DriveFileMoveOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DriveFileMoveOutline className="custom-class" />;

export const Clickable = () => <DriveFileMoveOutline onClick={()=>console.log("clicked")} />;

const Template = (args) => <DriveFileMoveOutline {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
