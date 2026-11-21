import React from "react";
import { Segment } from "./Segment";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Segment",
  component: Segment,
};

export const Default = () => <Segment />;
export const Fill = () => <Segment fill="blue" />;
export const Size = () => <Segment height="50" width="50" />;
export const CustomStyle = () => <Segment style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Segment className="custom-class" />;

export const Clickable = () => <Segment onClick={()=>console.log("clicked")} />;

const Template = (args) => <Segment {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
