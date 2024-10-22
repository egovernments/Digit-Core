import React from "react";
import { SpeakerPhone } from "./SpeakerPhone";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SpeakerPhone",
  component: SpeakerPhone,
};

export const Default = () => <SpeakerPhone />;
export const Fill = () => <SpeakerPhone fill="blue" />;
export const Size = () => <SpeakerPhone height="50" width="50" />;
export const CustomStyle = () => <SpeakerPhone style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SpeakerPhone className="custom-class" />;

export const Clickable = () => <SpeakerPhone onClick={()=>console.log("clicked")} />;

const Template = (args) => <SpeakerPhone {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
