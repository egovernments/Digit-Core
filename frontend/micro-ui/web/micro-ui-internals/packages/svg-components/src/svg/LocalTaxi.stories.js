import React from "react";
import { LocalTaxi } from "./LocalTaxi";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalTaxi",
  component: LocalTaxi,
};

export const Default = () => <LocalTaxi />;
export const Fill = () => <LocalTaxi fill="blue" />;
export const Size = () => <LocalTaxi height="50" width="50" />;
export const CustomStyle = () => <LocalTaxi style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalTaxi className="custom-class" />;

export const Clickable = () => <LocalTaxi onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalTaxi {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
