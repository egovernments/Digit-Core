import React from "react";
import { OfflineBolt } from "./OfflineBolt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "OfflineBolt",
  component: OfflineBolt,
};

export const Default = () => <OfflineBolt />;
export const Fill = () => <OfflineBolt fill="blue" />;
export const Size = () => <OfflineBolt height="50" width="50" />;
export const CustomStyle = () => <OfflineBolt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <OfflineBolt className="custom-class" />;

export const Clickable = () => <OfflineBolt onClick={()=>console.log("clicked")} />;

const Template = (args) => <OfflineBolt {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
