import React from "react";
import { AlarmOff } from "./AlarmOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AlarmOff",
  component: AlarmOff,
};

export const Default = () => <AlarmOff />;
export const Fill = () => <AlarmOff fill="blue" />;
export const Size = () => <AlarmOff height="50" width="50" />;
export const CustomStyle = () => <AlarmOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AlarmOff className="custom-class" />;
export const Clickable = () => <AlarmOff onClick={()=>console.log("clicked")} />;

const Template = (args) => <AlarmOff {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
