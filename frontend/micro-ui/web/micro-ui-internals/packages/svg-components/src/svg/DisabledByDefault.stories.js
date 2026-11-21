import React from "react";
import { DisabledByDefault } from "./DisabledByDefault";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DisabledByDefault",
  component: DisabledByDefault,
};

export const Default = () => <DisabledByDefault />;
export const Fill = () => <DisabledByDefault fill="blue" />;
export const Size = () => <DisabledByDefault height="50" width="50" />;
export const CustomStyle = () => <DisabledByDefault style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DisabledByDefault className="custom-class" />;

export const Clickable = () => <DisabledByDefault onClick={()=>console.log("clicked")} />;

const Template = (args) => <DisabledByDefault {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
