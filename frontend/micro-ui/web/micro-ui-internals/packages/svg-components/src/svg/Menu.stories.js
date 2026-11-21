import React from "react";
import { Menu } from "./Menu";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Menu",
  component: Menu,
};

export const Default = () => <Menu />;
export const Fill = () => <Menu fill="blue" />;
export const Size = () => <Menu height="50" width="50" />;
export const CustomStyle = () => <Menu style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Menu className="custom-class" />;

export const Clickable = () => <Menu onClick={()=>console.log("clicked")} />;

const Template = (args) => <Menu {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
