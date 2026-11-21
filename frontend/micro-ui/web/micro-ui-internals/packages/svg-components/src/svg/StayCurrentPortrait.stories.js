import React from "react";
import { StayCurrentPortrait } from "./StayCurrentPortrait";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "StayCurrentPortrait",
  component: StayCurrentPortrait,
};

export const Default = () => <StayCurrentPortrait />;
export const Fill = () => <StayCurrentPortrait fill="blue" />;
export const Size = () => <StayCurrentPortrait height="50" width="50" />;
export const CustomStyle = () => <StayCurrentPortrait style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <StayCurrentPortrait className="custom-class" />;

export const Clickable = () => <StayCurrentPortrait onClick={()=>console.log("clicked")} />;

const Template = (args) => <StayCurrentPortrait {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
