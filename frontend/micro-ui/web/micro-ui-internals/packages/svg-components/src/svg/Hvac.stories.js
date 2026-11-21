import React from "react";
import { Hvac } from "./Hvac";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Hvac",
  component: Hvac,
};

export const Default = () => <Hvac />;
export const Fill = () => <Hvac fill="blue" />;
export const Size = () => <Hvac height="50" width="50" />;
export const CustomStyle = () => <Hvac style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Hvac className="custom-class" />;

export const Clickable = () => <Hvac onClick={()=>console.log("clicked")} />;

const Template = (args) => <Hvac {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
