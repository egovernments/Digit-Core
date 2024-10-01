import React from "react";
import { PartyMode } from "./PartyMode";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PartyMode",
  component: PartyMode,
};

export const Default = () => <PartyMode />;
export const Fill = () => <PartyMode fill="blue" />;
export const Size = () => <PartyMode height="50" width="50" />;
export const CustomStyle = () => <PartyMode style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PartyMode className="custom-class" />;

export const Clickable = () => <PartyMode onClick={()=>console.log("clicked")} />;

const Template = (args) => <PartyMode {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
