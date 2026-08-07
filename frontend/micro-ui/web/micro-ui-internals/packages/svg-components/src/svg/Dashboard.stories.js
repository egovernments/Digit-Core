import React from "react";
import { Dashboard } from "./Dashboard";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Dashboard",
  component: Dashboard,
};

export const Default = () => <Dashboard />;
export const Fill = () => <Dashboard fill="blue" />;
export const Size = () => <Dashboard height="50" width="50" />;
export const CustomStyle = () => <Dashboard style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Dashboard className="custom-class" />;

export const Clickable = () => <Dashboard onClick={()=>console.log("clicked")} />;

const Template = (args) => <Dashboard {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
