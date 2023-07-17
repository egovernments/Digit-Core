import React from "react";
import { Logout } from "./Logout";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Logout",
  component: Logout,
};

export const Default = () => <Logout />;
export const Fill = () => <Logout fill="blue" />;
export const Size = () => <Logout height="50" width="50" />;
export const CustomStyle = () => <Logout style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Logout className="custom-class" />;

export const Clickable = () => <Logout onClick={()=>console.log("clicked")} />;

const Template = (args) => <Logout {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
