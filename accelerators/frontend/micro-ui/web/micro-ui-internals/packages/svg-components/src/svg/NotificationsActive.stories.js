import React from "react";
import { NotificationsActive } from "./NotificationsActive";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NotificationsActive",
  component: NotificationsActive,
};

export const Default = () => <NotificationsActive />;
export const Fill = () => <NotificationsActive fill="blue" />;
export const Size = () => <NotificationsActive height="50" width="50" />;
export const CustomStyle = () => <NotificationsActive style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NotificationsActive className="custom-class" />;

export const Clickable = () => <NotificationsActive onClick={()=>console.log("clicked")} />;

const Template = (args) => <NotificationsActive {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
