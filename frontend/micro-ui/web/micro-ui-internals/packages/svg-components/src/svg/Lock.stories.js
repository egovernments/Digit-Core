import React from "react";
import { Lock } from "./Lock";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Lock",
  component: Lock,
};

export const Default = () => <Lock />;
export const Fill = () => <Lock fill="blue" />;
export const Size = () => <Lock height="50" width="50" />;
export const CustomStyle = () => <Lock style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Lock className="custom-class" />;

export const Clickable = () => <Lock onClick={()=>console.log("clicked")} />;

const Template = (args) => <Lock {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
