import React from "react";
import { MenuOpen } from "./MenuOpen";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MenuOpen",
  component: MenuOpen,
};

export const Default = () => <MenuOpen />;
export const Fill = () => <MenuOpen fill="blue" />;
export const Size = () => <MenuOpen height="50" width="50" />;
export const CustomStyle = () => <MenuOpen style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MenuOpen className="custom-class" />;

export const Clickable = () => <MenuOpen onClick={()=>console.log("clicked")} />;

const Template = (args) => <MenuOpen {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
