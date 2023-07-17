import React from "react";
import { PersonAddSecondary } from "./PersonAddSecondary";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PersonAddSecondary",
  component: PersonAddSecondary,
};

export const Default = () => <PersonAddSecondary />;
export const Fill = () => <PersonAddSecondary fill="blue" />;
export const Size = () => <PersonAddSecondary height="50" width="50" />;
export const CustomStyle = () => <PersonAddSecondary style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PersonAddSecondary className="custom-class" />;

export const Clickable = () => <PersonAddSecondary onClick={()=>console.log("clicked")} />;

const Template = (args) => <PersonAddSecondary {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
