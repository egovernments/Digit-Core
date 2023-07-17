import React from "react";
import { SportsHandball } from "./SportsHandball";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SportsHandball",
  component: SportsHandball,
};

export const Default = () => <SportsHandball />;
export const Fill = () => <SportsHandball fill="blue" />;
export const Size = () => <SportsHandball height="50" width="50" />;
export const CustomStyle = () => <SportsHandball style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsHandball className="custom-class" />;

export const Clickable = () => <SportsHandball onClick={()=>console.log("clicked")} />;

const Template = (args) => <SportsHandball {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
