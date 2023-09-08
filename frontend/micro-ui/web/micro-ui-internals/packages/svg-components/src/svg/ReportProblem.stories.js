import React from "react";
import { ReportProblem } from "./ReportProblem";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ReportProblem",
  component: ReportProblem,
};

export const Default = () => <ReportProblem />;
export const Fill = () => <ReportProblem fill="blue" />;
export const Size = () => <ReportProblem height="50" width="50" />;
export const CustomStyle = () => <ReportProblem style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ReportProblem className="custom-class" />;

export const Clickable = () => <ReportProblem onClick={()=>console.log("clicked")} />;

const Template = (args) => <ReportProblem {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
