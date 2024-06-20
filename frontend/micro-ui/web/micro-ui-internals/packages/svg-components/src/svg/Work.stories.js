import React from "react";
import { Work } from "./Work";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Work",
  component: Work,
};

export const Default = () => <Work />;
export const Fill = () => <Work fill="blue" />;
export const Size = () => <Work height="50" width="50" />;
export const CustomStyle = () => <Work style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Work className="custom-class" />;
export const Clickable = () => <Work onClick={()=>console.log("clicked")} />;

const Template = (args) => <Work {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};