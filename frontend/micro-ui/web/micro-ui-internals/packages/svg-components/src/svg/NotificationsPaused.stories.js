import React from "react";
import { NotificationsPaused } from "./NotificationsPaused";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NotificationsPaused",
  component: NotificationsPaused,
};

export const Default = () => <NotificationsPaused />;
export const Fill = () => <NotificationsPaused fill="blue" />;
export const Size = () => <NotificationsPaused height="50" width="50" />;
export const CustomStyle = () => <NotificationsPaused style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NotificationsPaused className="custom-class" />;

export const Clickable = () => <NotificationsPaused onClick={()=>console.log("clicked")} />;

const Template = (args) => <NotificationsPaused {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
