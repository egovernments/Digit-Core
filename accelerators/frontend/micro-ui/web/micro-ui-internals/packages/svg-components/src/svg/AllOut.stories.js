import React from "react";
import { AllOut } from "./AllOut";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AllOut",
  component: AllOut,
};

export const Default = () => <AllOut />;
export const Fill = () => <AllOut fill="blue" />;
export const Size = () => <AllOut height="50" width="50" />;
export const CustomStyle = () => <AllOut style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AllOut className="custom-class" />;
export const Clickable = () => <AllOut onClick={()=>console.log("clicked")} />;

const Template = (args) => <AllOut {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
