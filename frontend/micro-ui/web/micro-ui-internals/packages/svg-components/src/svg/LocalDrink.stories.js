import React from "react";
import { LocalDrink } from "./LocalDrink";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalDrink",
  component: LocalDrink,
};

export const Default = () => <LocalDrink />;
export const Fill = () => <LocalDrink fill="blue" />;
export const Size = () => <LocalDrink height="50" width="50" />;
export const CustomStyle = () => <LocalDrink style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalDrink className="custom-class" />;

export const Clickable = () => <LocalDrink onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalDrink {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
