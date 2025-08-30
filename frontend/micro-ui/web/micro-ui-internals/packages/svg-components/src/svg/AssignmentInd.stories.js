import React from "react";
import { AssignmentInd } from "./AssignmentInd";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AssignmentInd",
  component: AssignmentInd,
};

export const Default = () => <AssignmentInd />;
export const Fill = () => <AssignmentInd fill="blue" />;
export const Size = () => <AssignmentInd height="50" width="50" />;
export const CustomStyle = () => <AssignmentInd style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AssignmentInd className="custom-class" />;
export const Clickable = () => <AssignmentInd onClick={()=>console.log("clicked")} />;

const Template = (args) => <AssignmentInd {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
