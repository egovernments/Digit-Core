import React from "react";
import { SportsHockey } from "./SportsHockey";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SportsHockey",
  component: SportsHockey,
};

export const Default = () => <SportsHockey />;
export const Fill = () => <SportsHockey fill="blue" />;
export const Size = () => <SportsHockey height="50" width="50" />;
export const CustomStyle = () => <SportsHockey style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsHockey className="custom-class" />;

export const Clickable = () => <SportsHockey onClick={()=>console.log("clicked")} />;

const Template = (args) => <SportsHockey {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
