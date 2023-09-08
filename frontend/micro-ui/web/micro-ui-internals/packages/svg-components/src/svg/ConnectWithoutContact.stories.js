import React from "react";
import { ConnectWithoutContact } from "./ConnectWithoutContact";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ConnectWithoutContact",
  component: ConnectWithoutContact,
};

export const Default = () => <ConnectWithoutContact />;
export const Fill = () => <ConnectWithoutContact fill="blue" />;
export const Size = () => <ConnectWithoutContact height="50" width="50" />;
export const CustomStyle = () => <ConnectWithoutContact style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ConnectWithoutContact className="custom-class" />;

export const Clickable = () => <ConnectWithoutContact onClick={()=>console.log("clicked")} />;

const Template = (args) => <ConnectWithoutContact {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
