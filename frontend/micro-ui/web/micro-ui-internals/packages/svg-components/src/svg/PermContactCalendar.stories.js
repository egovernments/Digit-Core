import React from "react";
import { PermContactCalendar } from "./PermContactCalendar";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PermContactCalendar",
  component: PermContactCalendar,
};

export const Default = () => <PermContactCalendar />;
export const Fill = () => <PermContactCalendar fill="blue" />;
export const Size = () => <PermContactCalendar height="50" width="50" />;
export const CustomStyle = () => <PermContactCalendar style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PermContactCalendar className="custom-class" />;

export const Clickable = () => <PermContactCalendar onClick={()=>console.log("clicked")} />;

const Template = (args) => <PermContactCalendar {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
