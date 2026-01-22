import React from "react";
import { Outbond } from "./Outbond";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Outbond",
  component: Outbond,
};

export const Default = () => <Outbond />;
export const Fill = () => <Outbond fill="blue" />;
export const Size = () => <Outbond height="50" width="50" />;
export const CustomStyle = () => <Outbond style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Outbond className="custom-class" />;

export const Clickable = () => <Outbond onClick={()=>console.log("clicked")} />;

const Template = (args) => <Outbond {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
