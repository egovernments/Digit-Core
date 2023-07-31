import React from "react";
import { Notifications } from "./Notifications";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Notifications",
  component: Notifications,
};

export const Default = () => <Notifications />;
export const Fill = () => <Notifications fill="blue" />;
export const Size = () => <Notifications height="50" width="50" />;
export const CustomStyle = () => <Notifications style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Notifications className="custom-class" />;

export const Clickable = () => <Notifications onClick={()=>console.log("clicked")} />;

const Template = (args) => <Notifications {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
