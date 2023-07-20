import React from "react";
import { SportsBaseball } from "./SportsBaseball";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SportsBaseball",
  component: SportsBaseball,
};

export const Default = () => <SportsBaseball />;
export const Fill = () => <SportsBaseball fill="blue" />;
export const Size = () => <SportsBaseball height="50" width="50" />;
export const CustomStyle = () => <SportsBaseball style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsBaseball className="custom-class" />;

export const Clickable = () => <SportsBaseball onClick={()=>console.log("clicked")} />;

const Template = (args) => <SportsBaseball {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
