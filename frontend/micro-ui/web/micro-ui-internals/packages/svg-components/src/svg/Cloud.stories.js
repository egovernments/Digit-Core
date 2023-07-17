import React from "react";
import { Cloud } from "./Cloud";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Cloud",
  component: Cloud,
};

export const Default = () => <Cloud />;
export const Fill = () => <Cloud fill="blue" />;
export const Size = () => <Cloud height="50" width="50" />;
export const CustomStyle = () => <Cloud style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Cloud className="custom-class" />;

export const Clickable = () => <Cloud onClick={()=>console.log("clicked")} />;

const Template = (args) => <Cloud {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
