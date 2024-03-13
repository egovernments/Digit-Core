import React from "react";
import { MenuBook } from "./MenuBook";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MenuBook",
  component: MenuBook,
};

export const Default = () => <MenuBook />;
export const Fill = () => <MenuBook fill="blue" />;
export const Size = () => <MenuBook height="50" width="50" />;
export const CustomStyle = () => <MenuBook style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MenuBook className="custom-class" />;

export const Clickable = () => <MenuBook onClick={()=>console.log("clicked")} />;

const Template = (args) => <MenuBook {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
