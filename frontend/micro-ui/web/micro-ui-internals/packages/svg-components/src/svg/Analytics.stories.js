import React from "react";
import { Analytics } from "./Analytics";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Analytics",
  component: Analytics,
};

export const Default = () => <Analytics />;
export const Fill = () => <Analytics fill="blue" />;
export const Size = () => <Analytics height="50" width="50" />;
export const CustomStyle = () => <Analytics style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Analytics className="custom-class" />;
export const Clickable = () => <Analytics onClick={()=>console.log("clicked")} />;

const Template = (args) => <Analytics {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
