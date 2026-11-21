import React from "react";
import { AssignmentReturned } from "./AssignmentReturned";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AssignmentReturned",
  component: AssignmentReturned,
};

export const Default = () => <AssignmentReturned />;
export const Fill = () => <AssignmentReturned fill="blue" />;
export const Size = () => <AssignmentReturned height="50" width="50" />;
export const CustomStyle = () => <AssignmentReturned style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AssignmentReturned className="custom-class" />;
export const Clickable = () => <AssignmentReturned onClick={()=>console.log("clicked")} />;

const Template = (args) => <AssignmentReturned {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
