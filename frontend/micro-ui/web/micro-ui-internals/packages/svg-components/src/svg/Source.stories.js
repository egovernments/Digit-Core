import React from "react";
import { Source } from "./Source";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Source",
  component: Source,
};

export const Default = () => <Source />;
export const Fill = () => <Source fill="blue" />;
export const Size = () => <Source height="50" width="50" />;
export const CustomStyle = () => <Source style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Source className="custom-class" />;

export const Clickable = () => <Source onClick={()=>console.log("clicked")} />;

const Template = (args) => <Source {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
