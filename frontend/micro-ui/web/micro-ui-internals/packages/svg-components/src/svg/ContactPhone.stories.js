import React from "react";
import { ContactPhone } from "./ContactPhone";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ContactPhone",
  component: ContactPhone,
};

export const Default = () => <ContactPhone />;
export const Fill = () => <ContactPhone fill="blue" />;
export const Size = () => <ContactPhone height="50" width="50" />;
export const CustomStyle = () => <ContactPhone style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ContactPhone className="custom-class" />;

export const Clickable = () => <ContactPhone onClick={()=>console.log("clicked")} />;

const Template = (args) => <ContactPhone {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
