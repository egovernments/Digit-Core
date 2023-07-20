import React from "react";
import { Navigation } from "./Navigation";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Navigation",
  component: Navigation,
};

export const Default = () => <Navigation />;
export const Fill = () => <Navigation fill="blue" />;
export const Size = () => <Navigation height="50" width="50" />;
export const CustomStyle = () => <Navigation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Navigation className="custom-class" />;

export const Clickable = () => <Navigation onClick={()=>console.log("clicked")} />;

const Template = (args) => <Navigation {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
