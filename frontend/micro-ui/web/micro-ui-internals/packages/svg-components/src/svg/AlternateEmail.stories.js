import React from "react";
import { AlternateEmail } from "./AlternateEmail";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AlternateEmail",
  component: AlternateEmail,
};

export const Default = () => <AlternateEmail />;
export const Fill = () => <AlternateEmail fill="blue" />;
export const Size = () => <AlternateEmail height="50" width="50" />;
export const CustomStyle = () => <AlternateEmail style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AlternateEmail className="custom-class" />;
export const Clickable = () => <AlternateEmail onClick={()=>console.log("clicked")} />;

const Template = (args) => <AlternateEmail {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
