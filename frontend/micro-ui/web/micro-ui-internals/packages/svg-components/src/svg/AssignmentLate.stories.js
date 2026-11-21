import React from "react";
import { AssignmentLate } from "./AssignmentLate";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AssignmentLate",
  component: AssignmentLate,
};

export const Default = () => <AssignmentLate />;
export const Fill = () => <AssignmentLate fill="blue" />;
export const Size = () => <AssignmentLate height="50" width="50" />;
export const CustomStyle = () => <AssignmentLate style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AssignmentLate className="custom-class" />;
export const Clickable = () => <AssignmentLate onClick={()=>console.log("clicked")} />;

const Template = (args) => <AssignmentLate {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
