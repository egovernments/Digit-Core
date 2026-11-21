import React from "react";
import { SetMeal } from "./SetMeal";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SetMeal",
  component: SetMeal,
};

export const Default = () => <SetMeal />;
export const Fill = () => <SetMeal fill="blue" />;
export const Size = () => <SetMeal height="50" width="50" />;
export const CustomStyle = () => <SetMeal style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SetMeal className="custom-class" />;

export const Clickable = () => <SetMeal onClick={()=>console.log("clicked")} />;

const Template = (args) => <SetMeal {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
