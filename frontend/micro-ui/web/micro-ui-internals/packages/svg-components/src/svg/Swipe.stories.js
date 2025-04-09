import React from "react";
import { Swipe } from "./Swipe";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Swipe",
  component: Swipe,
};

export const Default = () => <Swipe />;
export const Fill = () => <Swipe fill="blue" />;
export const Size = () => <Swipe height="50" width="50" />;
export const CustomStyle = () => <Swipe style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Swipe className="custom-class" />;
export const Clickable = () => <Swipe onClick={()=>console.log("clicked")} />;

const Template = (args) => <Swipe {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
