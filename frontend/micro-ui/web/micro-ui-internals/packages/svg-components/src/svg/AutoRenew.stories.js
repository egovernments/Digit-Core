import React from "react";
import { AutoRenew } from "./AutoRenew";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AutoRenew",
  component: AutoRenew,
};

export const Default = () => <AutoRenew />;
export const Fill = () => <AutoRenew fill="blue" />;
export const Size = () => <AutoRenew height="50" width="50" />;
export const CustomStyle = () => <AutoRenew style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AutoRenew className="custom-class" />;
export const Clickable = () => <AutoRenew onClick={()=>console.log("clicked")} />;

const Template = (args) => <AutoRenew {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
