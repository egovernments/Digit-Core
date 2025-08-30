import React from "react";
import { SupervisedUserCircle } from "./SupervisedUserCircle";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SupervisedUserCircle",
  component: SupervisedUserCircle,
};

export const Default = () => <SupervisedUserCircle />;
export const Fill = () => <SupervisedUserCircle fill="blue" />;
export const Size = () => <SupervisedUserCircle height="50" width="50" />;
export const CustomStyle = () => <SupervisedUserCircle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SupervisedUserCircle className="custom-class" />;

export const Clickable = () => <SupervisedUserCircle onClick={()=>console.log("clicked")} />;

const Template = (args) => <SupervisedUserCircle {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
