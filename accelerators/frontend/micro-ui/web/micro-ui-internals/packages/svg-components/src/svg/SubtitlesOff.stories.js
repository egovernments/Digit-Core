import React from "react";
import { SubtitlesOff } from "./SubtitlesOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SubtitlesOff",
  component: SubtitlesOff,
};

export const Default = () => <SubtitlesOff />;
export const Fill = () => <SubtitlesOff fill="blue" />;
export const Size = () => <SubtitlesOff height="50" width="50" />;
export const CustomStyle = () => <SubtitlesOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SubtitlesOff className="custom-class" />;

export const Clickable = () => <SubtitlesOff onClick={()=>console.log("clicked")} />;

const Template = (args) => <SubtitlesOff {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
