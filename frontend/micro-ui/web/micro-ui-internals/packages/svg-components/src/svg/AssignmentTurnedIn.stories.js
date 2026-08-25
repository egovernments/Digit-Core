import React from "react";
import { AssignmentTurnedIn } from "./AssignmentTurnedIn";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AssignmentTurnedIn",
  component: AssignmentTurnedIn,
};

export const Default = () => <AssignmentTurnedIn />;
export const Fill = () => <AssignmentTurnedIn fill="blue" />;
export const Size = () => <AssignmentTurnedIn height="50" width="50" />;
export const CustomStyle = () => <AssignmentTurnedIn style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AssignmentTurnedIn className="custom-class" />;
export const Clickable = () => <AssignmentTurnedIn onClick={()=>console.log("clicked")} />;

const Template = (args) => <AssignmentTurnedIn {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
