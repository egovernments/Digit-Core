import React from "react";
import { BugReport } from "./BugReport";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "BugReport",
  component: BugReport,
};

export const Default = () => <BugReport />;
export const Fill = () => <BugReport fill="blue" />;
export const Size = () => <BugReport height="50" width="50" />;
export const CustomStyle = () => <BugReport style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BugReport className="custom-class" />;

export const Clickable = () => <BugReport onClick={()=>console.log("clicked")} />;

const Template = (args) => <BugReport {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
