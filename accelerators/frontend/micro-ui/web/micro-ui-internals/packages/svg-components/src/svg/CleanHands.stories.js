import React from "react";
import { CleanHands } from "./CleanHands";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CleanHands",
  component: CleanHands,
};

export const Default = () => <CleanHands />;
export const Fill = () => <CleanHands fill="blue" />;
export const Size = () => <CleanHands height="50" width="50" />;
export const CustomStyle = () => <CleanHands style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CleanHands className="custom-class" />;

export const Clickable = () => <CleanHands onClick={()=>console.log("clicked")} />;

const Template = (args) => <CleanHands {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
