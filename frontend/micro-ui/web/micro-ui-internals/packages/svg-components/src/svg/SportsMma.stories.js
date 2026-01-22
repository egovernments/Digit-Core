import React from "react";
import { SportsMma } from "./SportsMma";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SportsMma",
  component: SportsMma,
};

export const Default = () => <SportsMma />;
export const Fill = () => <SportsMma fill="blue" />;
export const Size = () => <SportsMma height="50" width="50" />;
export const CustomStyle = () => <SportsMma style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsMma className="custom-class" />;

export const Clickable = () => <SportsMma onClick={()=>console.log("clicked")} />;

const Template = (args) => <SportsMma {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
