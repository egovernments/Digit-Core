import React from "react";
import { HorizontalSplit } from "./HorizontalSplit";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HorizontalSplit",
  component: HorizontalSplit,
};

export const Default = () => <HorizontalSplit />;
export const Fill = () => <HorizontalSplit fill="blue" />;
export const Size = () => <HorizontalSplit height="50" width="50" />;
export const CustomStyle = () => <HorizontalSplit style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HorizontalSplit className="custom-class" />;

export const Clickable = () => <HorizontalSplit onClick={()=>console.log("clicked")} />;

const Template = (args) => <HorizontalSplit {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
