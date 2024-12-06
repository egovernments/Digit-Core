import React from "react";
import { PermMedia } from "./PermMedia";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PermMedia",
  component: PermMedia,
};

export const Default = () => <PermMedia />;
export const Fill = () => <PermMedia fill="blue" />;
export const Size = () => <PermMedia height="50" width="50" />;
export const CustomStyle = () => <PermMedia style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PermMedia className="custom-class" />;

export const Clickable = () => <PermMedia onClick={()=>console.log("clicked")} />;

const Template = (args) => <PermMedia {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
