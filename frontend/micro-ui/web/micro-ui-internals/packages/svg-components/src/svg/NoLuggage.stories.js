import React from "react";
import { NoLuggage } from "./NoLuggage";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NoLuggage",
  component: NoLuggage,
};

export const Default = () => <NoLuggage />;
export const Fill = () => <NoLuggage fill="blue" />;
export const Size = () => <NoLuggage height="50" width="50" />;
export const CustomStyle = () => <NoLuggage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NoLuggage className="custom-class" />;

export const Clickable = () => <NoLuggage onClick={()=>console.log("clicked")} />;

const Template = (args) => <NoLuggage {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
