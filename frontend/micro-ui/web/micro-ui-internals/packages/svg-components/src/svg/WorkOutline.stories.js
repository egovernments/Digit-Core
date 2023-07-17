import React from "react";
import { WorkOutline } from "./WorkOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "WorkOutline",
  component: WorkOutline,
};

export const Default = () => <WorkOutline />;
export const Fill = () => <WorkOutline fill="blue" />;
export const Size = () => <WorkOutline height="50" width="50" />;
export const CustomStyle = () => <WorkOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WorkOutline className="custom-class" />;
export const Clickable = () => <WorkOutline onClick={()=>console.log("clicked")} />;

const Template = (args) => <WorkOutline {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
