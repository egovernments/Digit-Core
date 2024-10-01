import React from "react";
import { Event } from "./Event";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Event",
  component: Event,
};

export const Default = () => <Event />;
export const Fill = () => <Event fill="blue" />;
export const Size = () => <Event height="50" width="50" />;
export const CustomStyle = () => <Event style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Event className="custom-class" />;

export const Clickable = () => <Event onClick={()=>console.log("clicked")} />;

const Template = (args) => <Event {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
