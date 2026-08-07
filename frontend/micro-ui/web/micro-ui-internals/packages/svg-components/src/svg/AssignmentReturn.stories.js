import React from "react";
import { AssignmentReturn } from "./AssignmentReturn";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AssignmentReturn",
  component: AssignmentReturn,
};

export const Default = () => <AssignmentReturn />;
export const Fill = () => <AssignmentReturn fill="blue" />;
export const Size = () => <AssignmentReturn height="50" width="50" />;
export const CustomStyle = () => <AssignmentReturn style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AssignmentReturn className="custom-class" />;
export const Clickable = () => <AssignmentReturn onClick={()=>console.log("clicked")} />;

const Template = (args) => <AssignmentReturn {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
