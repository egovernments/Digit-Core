import React from "react";
import { NotificationsNone } from "./NotificationsNone";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NotificationsNone",
  component: NotificationsNone,
};

export const Default = () => <NotificationsNone />;
export const Fill = () => <NotificationsNone fill="blue" />;
export const Size = () => <NotificationsNone height="50" width="50" />;
export const CustomStyle = () => <NotificationsNone style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NotificationsNone className="custom-class" />;

export const Clickable = () => <NotificationsNone onClick={()=>console.log("clicked")} />;

const Template = (args) => <NotificationsNone {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
