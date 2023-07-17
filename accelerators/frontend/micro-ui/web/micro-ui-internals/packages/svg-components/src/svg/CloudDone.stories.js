import React from "react";
import { CloudDone } from "./CloudDone";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CloudDone",
  component: CloudDone,
};

export const Default = () => <CloudDone />;
export const Fill = () => <CloudDone fill="blue" />;
export const Size = () => <CloudDone height="50" width="50" />;
export const CustomStyle = () => <CloudDone style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CloudDone className="custom-class" />;

export const Clickable = () => <CloudDone onClick={()=>console.log("clicked")} />;

const Template = (args) => <CloudDone {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
