import React from "react";
import { NightlightRound } from "./NightlightRound";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NightlightRound",
  component: NightlightRound,
};

export const Default = () => <NightlightRound />;
export const Fill = () => <NightlightRound fill="blue" />;
export const Size = () => <NightlightRound height="50" width="50" />;
export const CustomStyle = () => <NightlightRound style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NightlightRound className="custom-class" />;

export const Clickable = () => <NightlightRound onClick={()=>console.log("clicked")} />;

const Template = (args) => <NightlightRound {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
