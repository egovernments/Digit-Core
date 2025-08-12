import React from "react";
import { CallMissedOutgoing } from "./CallMissedOutgoing";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CallMissedOutgoing",
  component: CallMissedOutgoing,
};

export const Default = () => <CallMissedOutgoing />;
export const Fill = () => <CallMissedOutgoing fill="blue" />;
export const Size = () => <CallMissedOutgoing height="50" width="50" />;
export const CustomStyle = () => <CallMissedOutgoing style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CallMissedOutgoing className="custom-class" />;

export const Clickable = () => <CallMissedOutgoing onClick={()=>console.log("clicked")} />;

const Template = (args) => <CallMissedOutgoing {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
