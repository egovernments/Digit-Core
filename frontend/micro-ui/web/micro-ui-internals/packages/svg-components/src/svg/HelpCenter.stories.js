import React from "react";
import { HelpCenter } from "./HelpCenter";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HelpCenter",
  component: HelpCenter,
};

export const Default = () => <HelpCenter />;
export const Fill = () => <HelpCenter fill="blue" />;
export const Size = () => <HelpCenter height="50" width="50" />;
export const CustomStyle = () => <HelpCenter style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HelpCenter className="custom-class" />;

export const Clickable = () => <HelpCenter onClick={()=>console.log("clicked")} />;

const Template = (args) => <HelpCenter {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
