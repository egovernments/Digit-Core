import React from "react";
import { ViewDay } from "./ViewDay";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ViewDay",
  component: ViewDay,
};

export const Default = () => <ViewDay />;
export const Fill = () => <ViewDay fill="blue" />;
export const Size = () => <ViewDay height="50" width="50" />;
export const CustomStyle = () => <ViewDay style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewDay className="custom-class" />;
export const Clickable = () => <ViewDay onClick={()=>console.log("clicked")} />;

const Template = (args) => <ViewDay {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
