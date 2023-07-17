import React from "react";
import { StoreMallDirectory } from "./StoreMallDirectory";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "StoreMallDirectory",
  component: StoreMallDirectory,
};

export const Default = () => <StoreMallDirectory />;
export const Fill = () => <StoreMallDirectory fill="blue" />;
export const Size = () => <StoreMallDirectory height="50" width="50" />;
export const CustomStyle = () => <StoreMallDirectory style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <StoreMallDirectory className="custom-class" />;

export const Clickable = () => <StoreMallDirectory onClick={()=>console.log("clicked")} />;

const Template = (args) => <StoreMallDirectory {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
