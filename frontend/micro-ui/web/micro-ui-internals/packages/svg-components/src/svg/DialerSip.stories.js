import React from "react";
import { DialerSip } from "./DialerSip";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DialerSip",
  component: DialerSip,
};

export const Default = () => <DialerSip />;
export const Fill = () => <DialerSip fill="blue" />;
export const Size = () => <DialerSip height="50" width="50" />;
export const CustomStyle = () => <DialerSip style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DialerSip className="custom-class" />;

export const Clickable = () => <DialerSip onClick={()=>console.log("clicked")} />;

const Template = (args) => <DialerSip {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
