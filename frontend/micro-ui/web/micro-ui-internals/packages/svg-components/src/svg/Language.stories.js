import React from "react";
import { Language } from "./Language";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Language",
  component: Language,
};

export const Default = () => <Language />;
export const Fill = () => <Language fill="blue" />;
export const Size = () => <Language height="50" width="50" />;
export const CustomStyle = () => <Language style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Language className="custom-class" />;

export const Clickable = () => <Language onClick={()=>console.log("clicked")} />;

const Template = (args) => <Language {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
