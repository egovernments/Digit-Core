import React from "react";
import { PendingActions } from "./PendingActions";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PendingActions",
  component: PendingActions,
};

export const Default = () => <PendingActions />;
export const Fill = () => <PendingActions fill="blue" />;
export const Size = () => <PendingActions height="50" width="50" />;
export const CustomStyle = () => <PendingActions style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PendingActions className="custom-class" />;

export const Clickable = () => <PendingActions onClick={()=>console.log("clicked")} />;

const Template = (args) => <PendingActions {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
