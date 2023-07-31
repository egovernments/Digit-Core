import React from "react";
import { CalendarToday } from "./CalendarToday";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CalendarToday",
  component: CalendarToday,
};

export const Default = () => <CalendarToday />;
export const Fill = () => <CalendarToday fill="blue" />;
export const Size = () => <CalendarToday height="50" width="50" />;
export const CustomStyle = () => <CalendarToday style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CalendarToday className="custom-class" />;

export const Clickable = () => <CalendarToday onClick={()=>console.log("clicked")} />;

const Template = (args) => <CalendarToday {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
