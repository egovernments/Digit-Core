import React from "react";
import { CircleNotifications } from "./CircleNotifications";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CircleNotifications",
  component: CircleNotifications,
};

export const Default = () => <CircleNotifications />;
export const Fill = () => <CircleNotifications fill="blue" />;
export const Size = () => <CircleNotifications height="50" width="50" />;
export const CustomStyle = () => <CircleNotifications style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CircleNotifications className="custom-class" />;

export const Clickable = () => <CircleNotifications onClick={()=>console.log("clicked")} />;

const Template = (args) => <CircleNotifications {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
