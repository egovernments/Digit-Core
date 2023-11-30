import React from "react";
import { VoiceOverOff } from "./VoiceOverOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "VoiceOverOff",
  component: VoiceOverOff,
};

export const Default = () => <VoiceOverOff />;
export const Fill = () => <VoiceOverOff fill="blue" />;
export const Size = () => <VoiceOverOff height="50" width="50" />;
export const CustomStyle = () => <VoiceOverOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <VoiceOverOff className="custom-class" />;
export const Clickable = () => <VoiceOverOff onClick={()=>console.log("clicked")} />;

const Template = (args) => <VoiceOverOff {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
