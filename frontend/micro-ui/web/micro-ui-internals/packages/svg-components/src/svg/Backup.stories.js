import React from "react";
import { Backup } from "./Backup";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Backup",
  component: Backup,
};

export const Default = () => <Backup />;
export const Fill = () => <Backup fill="blue" />;
export const Size = () => <Backup height="50" width="50" />;
export const CustomStyle = () => <Backup style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Backup className="custom-class" />;

export const Clickable = () => <Backup onClick={()=>console.log("clicked")} />;

const Template = (args) => <Backup {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
