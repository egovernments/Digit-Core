import React from "react";
import { Engineering } from "./Engineering";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Engineering",
  component: Engineering,
};

export const Default = () => <Engineering />;
export const Fill = () => <Engineering fill="blue" />;
export const Size = () => <Engineering height="50" width="50" />;
export const CustomStyle = () => <Engineering style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Engineering className="custom-class" />;

export const Clickable = () => <Engineering onClick={()=>console.log("clicked")} />;

const Template = (args) => <Engineering {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
