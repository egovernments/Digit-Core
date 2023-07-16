import React from "react";
import { Voicemail } from "./Voicemail";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Voicemail",
  component: Voicemail,
};

export const Default = () => <Voicemail />;
export const Fill = () => <Voicemail fill="blue" />;
export const Size = () => <Voicemail height="50" width="50" />;
export const CustomStyle = () => <Voicemail style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Voicemail className="custom-class" />;
export const Clickable = () => <Voicemail onClick={()=>console.log("clicked")} />;

const Template = (args) => <Voicemail {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
