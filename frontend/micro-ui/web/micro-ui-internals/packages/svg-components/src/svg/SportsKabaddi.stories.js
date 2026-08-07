import React from "react";
import { SportsKabaddi } from "./SportsKabaddi";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SportsKabaddi",
  component: SportsKabaddi,
};

export const Default = () => <SportsKabaddi />;
export const Fill = () => <SportsKabaddi fill="blue" />;
export const Size = () => <SportsKabaddi height="50" width="50" />;
export const CustomStyle = () => <SportsKabaddi style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsKabaddi className="custom-class" />;

export const Clickable = () => <SportsKabaddi onClick={()=>console.log("clicked")} />;

const Template = (args) => <SportsKabaddi {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
