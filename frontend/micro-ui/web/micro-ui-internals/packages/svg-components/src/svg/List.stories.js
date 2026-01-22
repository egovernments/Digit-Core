import React from "react";
import { List } from "./List";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "List",
  component: List,
};

export const Default = () => <List />;
export const Fill = () => <List fill="blue" />;
export const Size = () => <List height="50" width="50" />;
export const CustomStyle = () => <List style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <List className="custom-class" />;

export const Clickable = () => <List onClick={()=>console.log("clicked")} />;

const Template = (args) => <List {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
