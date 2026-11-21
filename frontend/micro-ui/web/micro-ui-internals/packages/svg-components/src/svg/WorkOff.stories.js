import React from "react";
import { WorkOff } from "./WorkOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "WorkOff",
  component: WorkOff,
};

export const Default = () => <WorkOff />;
export const Fill = () => <WorkOff fill="blue" />;
export const Size = () => <WorkOff height="50" width="50" />;
export const CustomStyle = () => <WorkOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WorkOff className="custom-class" />;
export const Clickable = () => <WorkOff onClick={()=>console.log("clicked")} />;

const Template = (args) => <WorkOff {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};