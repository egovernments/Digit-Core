import React from "react";
import { Group } from "./Group";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Group",
  component: Group,
};

export const Default = () => <Group />;
export const Fill = () => <Group fill="blue" />;
export const Size = () => <Group height="50" width="50" />;
export const CustomStyle = () => <Group style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Group className="custom-class" />;

export const Clickable = () => <Group onClick={()=>console.log("clicked")} />;

const Template = (args) => <Group {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
