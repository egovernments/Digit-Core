import React from "react";
import { AttachEmail } from "./AttachEmail";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AttachEmail",
  component: AttachEmail,
};

export const Default = () => <AttachEmail />;
export const Fill = () => <AttachEmail fill="blue" />;
export const Size = () => <AttachEmail height="50" width="50" />;
export const CustomStyle = () => <AttachEmail style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AttachEmail className="custom-class" />;
export const Clickable = () => <AttachEmail onClick={()=>console.log("clicked")} />;

const Template = (args) => <AttachEmail {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
