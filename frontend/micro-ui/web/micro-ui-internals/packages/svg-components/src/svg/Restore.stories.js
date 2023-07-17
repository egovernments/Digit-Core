import React from "react";
import { Restore } from "./Restore";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Restore",
  component: Restore,
};

export const Default = () => <Restore />;
export const Fill = () => <Restore fill="blue" />;
export const Size = () => <Restore height="50" width="50" />;
export const CustomStyle = () => <Restore style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Restore className="custom-class" />;

export const Clickable = () => <Restore onClick={()=>console.log("clicked")} />;

const Template = (args) => <Restore {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
