import React from "react";
import { Commute } from "./Commute";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Commute",
  component: Commute,
};

export const Default = () => <Commute />;
export const Fill = () => <Commute fill="blue" />;
export const Size = () => <Commute height="50" width="50" />;
export const CustomStyle = () => <Commute style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Commute className="custom-class" />;

export const Clickable = () => <Commute onClick={()=>console.log("clicked")} />;

const Template = (args) => <Commute {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
