import React from "react";
import { LocalAtm } from "./LocalAtm";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalAtm",
  component: LocalAtm,
};

export const Default = () => <LocalAtm />;
export const Fill = () => <LocalAtm fill="blue" />;
export const Size = () => <LocalAtm height="50" width="50" />;
export const CustomStyle = () => <LocalAtm style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalAtm className="custom-class" />;

export const Clickable = () => <LocalAtm onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalAtm {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
