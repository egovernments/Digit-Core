import React from "react";
import { SwitchAccount } from "./SwitchAccount";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SwitchAccount",
  component: SwitchAccount,
};

export const Default = () => <SwitchAccount />;
export const Fill = () => <SwitchAccount fill="blue" />;
export const Size = () => <SwitchAccount height="50" width="50" />;
export const CustomStyle = () => <SwitchAccount style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwitchAccount className="custom-class" />;
export const Clickable = () => <SwitchAccount onClick={()=>console.log("clicked")} />;

const Template = (args) => <SwitchAccount {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
