import React from "react";
import { ChromeReaderMode } from "./ChromeReaderMode";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ChromeReaderMode",
  component: ChromeReaderMode,
};

export const Default = () => <ChromeReaderMode />;
export const Fill = () => <ChromeReaderMode fill="blue" />;
export const Size = () => <ChromeReaderMode height="50" width="50" />;
export const CustomStyle = () => <ChromeReaderMode style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ChromeReaderMode className="custom-class" />;

export const Clickable = () => <ChromeReaderMode onClick={()=>console.log("clicked")} />;

const Template = (args) => <ChromeReaderMode {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
