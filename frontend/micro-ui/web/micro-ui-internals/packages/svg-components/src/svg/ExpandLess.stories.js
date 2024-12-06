import React from "react";
import { ExpandLess } from "./ExpandLess";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ExpandLess",
  component: ExpandLess,
};

export const Default = () => <ExpandLess />;
export const Fill = () => <ExpandLess fill="blue" />;
export const Size = () => <ExpandLess height="50" width="50" />;
export const CustomStyle = () => <ExpandLess style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ExpandLess className="custom-class" />;

export const Clickable = () => <ExpandLess onClick={()=>console.log("clicked")} />;

const Template = (args) => <ExpandLess {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
