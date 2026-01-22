import React from "react";
import { Approval } from "./Approval";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Approval",
  component: Approval,
};

export const Default = () => <Approval />;
export const Fill = () => <Approval fill="blue" />;
export const Size = () => <Approval height="50" width="50" />;
export const CustomStyle = () => <Approval style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Approval className="custom-class" />;
export const Clickable = () => <Approval onClick={()=>console.log("clicked")} />;

const Template = (args) => <Approval {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
