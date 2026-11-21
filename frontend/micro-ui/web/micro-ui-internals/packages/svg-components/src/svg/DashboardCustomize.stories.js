import React from "react";
import { DashboardCustomize } from "./DashboardCustomize";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DashboardCustomize",
  component: DashboardCustomize,
};

export const Default = () => <DashboardCustomize />;
export const Fill = () => <DashboardCustomize fill="blue" />;
export const Size = () => <DashboardCustomize height="50" width="50" />;
export const CustomStyle = () => <DashboardCustomize style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DashboardCustomize className="custom-class" />;

export const Clickable = () => <DashboardCustomize onClick={()=>console.log("clicked")} />;

const Template = (args) => <DashboardCustomize {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
