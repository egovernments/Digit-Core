import React from "react";
import { Coronavirus } from "./Coronavirus";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Coronavirus",
  component: Coronavirus,
};

export const Default = () => <Coronavirus />;
export const Fill = () => <Coronavirus fill="blue" />;
export const Size = () => <Coronavirus height="50" width="50" />;
export const CustomStyle = () => <Coronavirus style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Coronavirus className="custom-class" />;

export const Clickable = () => <Coronavirus onClick={()=>console.log("clicked")} />;

const Template = (args) => <Coronavirus {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
