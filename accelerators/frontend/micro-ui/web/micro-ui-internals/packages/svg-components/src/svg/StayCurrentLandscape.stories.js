import React from "react";
import { StayCurrentLandscape } from "./StayCurrentLandscape";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "StayCurrentLandscape",
  component: StayCurrentLandscape,
};

export const Default = () => <StayCurrentLandscape />;
export const Fill = () => <StayCurrentLandscape fill="blue" />;
export const Size = () => <StayCurrentLandscape height="50" width="50" />;
export const CustomStyle = () => <StayCurrentLandscape style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <StayCurrentLandscape className="custom-class" />;

export const Clickable = () => <StayCurrentLandscape onClick={()=>console.log("clicked")} />;

const Template = (args) => <StayCurrentLandscape {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
