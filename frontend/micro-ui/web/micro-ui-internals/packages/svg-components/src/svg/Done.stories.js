import React from "react";
import { Done } from "./Done";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Done",
  component: Done,
};

export const Default = () => <Done />;
export const Fill = () => <Done fill="blue" />;
export const Size = () => <Done height="50" width="50" />;
export const CustomStyle = () => <Done style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Done className="custom-class" />;

export const Clickable = () => <Done onClick={()=>console.log("clicked")} />;

const Template = (args) => <Done {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
