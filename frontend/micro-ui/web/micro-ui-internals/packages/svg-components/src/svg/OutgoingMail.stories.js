import React from "react";
import { OutgoingMail } from "./OutgoingMail";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "OutgoingMail",
  component: OutgoingMail,
};

export const Default = () => <OutgoingMail />;
export const Fill = () => <OutgoingMail fill="blue" />;
export const Size = () => <OutgoingMail height="50" width="50" />;
export const CustomStyle = () => <OutgoingMail style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <OutgoingMail className="custom-class" />;

export const Clickable = () => <OutgoingMail onClick={()=>console.log("clicked")} />;

const Template = (args) => <OutgoingMail {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
