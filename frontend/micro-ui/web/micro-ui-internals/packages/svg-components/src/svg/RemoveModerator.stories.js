import React from "react";
import { RemoveModerator } from "./RemoveModerator";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RemoveModerator",
  component: RemoveModerator,
};

export const Default = () => <RemoveModerator />;
export const Fill = () => <RemoveModerator fill="blue" />;
export const Size = () => <RemoveModerator height="50" width="50" />;
export const CustomStyle = () => <RemoveModerator style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RemoveModerator className="custom-class" />;

export const Clickable = () => <RemoveModerator onClick={()=>console.log("clicked")} />;

const Template = (args) => <RemoveModerator {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
