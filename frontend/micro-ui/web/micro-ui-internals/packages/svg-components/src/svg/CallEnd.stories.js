import React from "react";
import { CallEnd } from "./CallEnd";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CallEnd",
  component: CallEnd,
};

export const Default = () => <CallEnd />;
export const Fill = () => <CallEnd fill="blue" />;
export const Size = () => <CallEnd height="50" width="50" />;
export const CustomStyle = () => <CallEnd style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CallEnd className="custom-class" />;

export const Clickable = () => <CallEnd onClick={()=>console.log("clicked")} />;

const Template = (args) => <CallEnd {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
