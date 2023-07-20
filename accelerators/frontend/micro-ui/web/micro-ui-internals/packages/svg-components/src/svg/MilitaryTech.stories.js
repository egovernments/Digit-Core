import React from "react";
import { MilitaryTech } from "./MilitaryTech";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MilitaryTech",
  component: MilitaryTech,
};

export const Default = () => <MilitaryTech />;
export const Fill = () => <MilitaryTech fill="blue" />;
export const Size = () => <MilitaryTech height="50" width="50" />;
export const CustomStyle = () => <MilitaryTech style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MilitaryTech className="custom-class" />;

export const Clickable = () => <MilitaryTech onClick={()=>console.log("clicked")} />;

const Template = (args) => <MilitaryTech {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
