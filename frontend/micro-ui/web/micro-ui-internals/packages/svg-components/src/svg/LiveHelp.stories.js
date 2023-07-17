import React from "react";
import { LiveHelp } from "./LiveHelp";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LiveHelp",
  component: LiveHelp,
};

export const Default = () => <LiveHelp />;
export const Fill = () => <LiveHelp fill="blue" />;
export const Size = () => <LiveHelp height="50" width="50" />;
export const CustomStyle = () => <LiveHelp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LiveHelp className="custom-class" />;

export const Clickable = () => <LiveHelp onClick={()=>console.log("clicked")} />;

const Template = (args) => <LiveHelp {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
