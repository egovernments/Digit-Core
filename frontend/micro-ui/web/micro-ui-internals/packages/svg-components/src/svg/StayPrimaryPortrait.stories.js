import React from "react";
import { StayPrimaryPortrait } from "./StayPrimaryPortrait";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "StayPrimaryPortrait",
  component: StayPrimaryPortrait,
};

export const Default = () => <StayPrimaryPortrait />;
export const Fill = () => <StayPrimaryPortrait fill="blue" />;
export const Size = () => <StayPrimaryPortrait height="50" width="50" />;
export const CustomStyle = () => <StayPrimaryPortrait style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <StayPrimaryPortrait className="custom-class" />;

export const Clickable = () => <StayPrimaryPortrait onClick={()=>console.log("clicked")} />;

const Template = (args) => <StayPrimaryPortrait {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
