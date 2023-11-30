import React from "react";
import { Mood } from "./Mood";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Mood",
  component: Mood,
};

export const Default = () => <Mood />;
export const Fill = () => <Mood fill="blue" />;
export const Size = () => <Mood height="50" width="50" />;
export const CustomStyle = () => <Mood style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Mood className="custom-class" />;

export const Clickable = () => <Mood onClick={()=>console.log("clicked")} />;

const Template = (args) => <Mood {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
