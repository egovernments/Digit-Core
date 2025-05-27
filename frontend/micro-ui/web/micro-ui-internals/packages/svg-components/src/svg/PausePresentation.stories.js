import React from "react";
import { PausePresentation } from "./PausePresentation";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PausePresentation",
  component: PausePresentation,
};

export const Default = () => <PausePresentation />;
export const Fill = () => <PausePresentation fill="blue" />;
export const Size = () => <PausePresentation height="50" width="50" />;
export const CustomStyle = () => <PausePresentation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PausePresentation className="custom-class" />;

export const Clickable = () => <PausePresentation onClick={()=>console.log("clicked")} />;

const Template = (args) => <PausePresentation {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
