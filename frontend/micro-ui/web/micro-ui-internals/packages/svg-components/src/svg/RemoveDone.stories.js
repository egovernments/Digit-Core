import React from "react";
import { RemoveDone } from "./RemoveDone";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RemoveDone",
  component: RemoveDone,
};

export const Default = () => <RemoveDone />;
export const Fill = () => <RemoveDone fill="blue" />;
export const Size = () => <RemoveDone height="50" width="50" />;
export const CustomStyle = () => <RemoveDone style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RemoveDone className="custom-class" />;

export const Clickable = () => <RemoveDone onClick={()=>console.log("clicked")} />;

const Template = (args) => <RemoveDone {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
