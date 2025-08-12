import React from "react";
import { Outlet } from "./Outlet";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Outlet",
  component: Outlet,
};

export const Default = () => <Outlet />;
export const Fill = () => <Outlet fill="blue" />;
export const Size = () => <Outlet height="50" width="50" />;
export const CustomStyle = () => <Outlet style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Outlet className="custom-class" />;

export const Clickable = () => <Outlet onClick={()=>console.log("clicked")} />;

const Template = (args) => <Outlet {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
