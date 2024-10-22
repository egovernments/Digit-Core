import React from "react";
import { MoodBad } from "./MoodBad";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MoodBad",
  component: MoodBad,
};

export const Default = () => <MoodBad />;
export const Fill = () => <MoodBad fill="blue" />;
export const Size = () => <MoodBad height="50" width="50" />;
export const CustomStyle = () => <MoodBad style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MoodBad className="custom-class" />;

export const Clickable = () => <MoodBad onClick={()=>console.log("clicked")} />;

const Template = (args) => <MoodBad {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
