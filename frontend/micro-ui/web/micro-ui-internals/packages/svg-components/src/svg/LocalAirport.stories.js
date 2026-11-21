import React from "react";
import { LocalAirport } from "./LocalAirport";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalAirport",
  component: LocalAirport,
};

export const Default = () => <LocalAirport />;
export const Fill = () => <LocalAirport fill="blue" />;
export const Size = () => <LocalAirport height="50" width="50" />;
export const CustomStyle = () => <LocalAirport style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalAirport className="custom-class" />;

export const Clickable = () => <LocalAirport onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalAirport {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
