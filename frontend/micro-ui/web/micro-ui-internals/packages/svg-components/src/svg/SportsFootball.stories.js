import React from "react";
import { SportsFootball } from "./SportsFootball";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SportsFootball",
  component: SportsFootball,
};

export const Default = () => <SportsFootball />;
export const Fill = () => <SportsFootball fill="blue" />;
export const Size = () => <SportsFootball height="50" width="50" />;
export const CustomStyle = () => <SportsFootball style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsFootball className="custom-class" />;

export const Clickable = () => <SportsFootball onClick={()=>console.log("clicked")} />;

const Template = (args) => <SportsFootball {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
