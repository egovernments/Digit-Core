import React from "react";
import { CloudDownload } from "./CloudDownload";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CloudDownload",
  component: CloudDownload,
};

export const Default = () => <CloudDownload />;
export const Fill = () => <CloudDownload fill="blue" />;
export const Size = () => <CloudDownload height="50" width="50" />;
export const CustomStyle = () => <CloudDownload style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CloudDownload className="custom-class" />;

export const Clickable = () => <CloudDownload onClick={()=>console.log("clicked")} />;

const Template = (args) => <CloudDownload {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
