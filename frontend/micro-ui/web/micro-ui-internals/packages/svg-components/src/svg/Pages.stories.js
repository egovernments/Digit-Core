import React from "react";
import { Pages } from "./Pages";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Pages",
  component: Pages,
};

export const Default = () => <Pages />;
export const Fill = () => <Pages fill="blue" />;
export const Size = () => <Pages height="50" width="50" />;
export const CustomStyle = () => <Pages style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Pages className="custom-class" />;

export const Clickable = () => <Pages onClick={()=>console.log("clicked")} />;

const Template = (args) => <Pages {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
