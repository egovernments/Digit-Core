import React from "react";
import { HistoryEdu } from "./HistoryEdu";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HistoryEdu",
  component: HistoryEdu,
};

export const Default = () => <HistoryEdu />;
export const Fill = () => <HistoryEdu fill="blue" />;
export const Size = () => <HistoryEdu height="50" width="50" />;
export const CustomStyle = () => <HistoryEdu style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HistoryEdu className="custom-class" />;

export const Clickable = () => <HistoryEdu onClick={()=>console.log("clicked")} />;

const Template = (args) => <HistoryEdu {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
