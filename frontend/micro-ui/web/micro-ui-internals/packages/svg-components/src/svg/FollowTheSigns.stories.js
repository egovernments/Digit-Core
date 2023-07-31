import React from "react";
import { FollowTheSigns } from "./FollowTheSigns";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FollowTheSigns",
  component: FollowTheSigns,
};

export const Default = () => <FollowTheSigns />;
export const Fill = () => <FollowTheSigns fill="blue" />;
export const Size = () => <FollowTheSigns height="50" width="50" />;
export const CustomStyle = () => <FollowTheSigns style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FollowTheSigns className="custom-class" />;

export const Clickable = () => <FollowTheSigns onClick={()=>console.log("clicked")} />;

const Template = (args) => <FollowTheSigns {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
