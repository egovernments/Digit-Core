import React from "react";
import { LocalMall } from "./LocalMall";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalMall",
  component: LocalMall,
};

export const Default = () => <LocalMall />;
export const Fill = () => <LocalMall fill="blue" />;
export const Size = () => <LocalMall height="50" width="50" />;
export const CustomStyle = () => <LocalMall style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalMall className="custom-class" />;

export const Clickable = () => <LocalMall onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalMall {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
