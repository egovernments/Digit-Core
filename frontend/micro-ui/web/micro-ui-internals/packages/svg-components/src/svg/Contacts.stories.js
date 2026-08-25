import React from "react";
import { Contacts } from "./Contacts";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Contacts",
  component: Contacts,
};

export const Default = () => <Contacts />;
export const Fill = () => <Contacts fill="blue" />;
export const Size = () => <Contacts height="50" width="50" />;
export const CustomStyle = () => <Contacts style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Contacts className="custom-class" />;

export const Clickable = () => <Contacts onClick={()=>console.log("clicked")} />;

const Template = (args) => <Contacts {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
