import React from "react";
import { SpeakerNotes } from "./SpeakerNotes";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SpeakerNotes",
  component: SpeakerNotes,
};

export const Default = () => <SpeakerNotes />;
export const Fill = () => <SpeakerNotes fill="blue" />;
export const Size = () => <SpeakerNotes height="50" width="50" />;
export const CustomStyle = () => <SpeakerNotes style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SpeakerNotes className="custom-class" />;

export const Clickable = () => <SpeakerNotes onClick={()=>console.log("clicked")} />;

const Template = (args) => <SpeakerNotes {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
