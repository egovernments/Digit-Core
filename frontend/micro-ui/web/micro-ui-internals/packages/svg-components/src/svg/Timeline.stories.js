import React from "react";
import { Timeline } from "./Timeline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Timeline",
  component: Timeline,
};

export const Default = () => <Timeline />;
export const Fill = () => <Timeline fill="blue" />;
export const Size = () => <Timeline height="50" width="50" />;
export const CustomStyle = () => <Timeline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Timeline className="custom-class" />;
export const Clickable = () => <Timeline onClick={()=>console.log("clicked")} />;

const Template = (args) => <Timeline {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
