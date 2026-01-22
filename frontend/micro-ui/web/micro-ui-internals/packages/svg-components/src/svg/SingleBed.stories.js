import React from "react";
import { SingleBed } from "./SingleBed";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SingleBed",
  component: SingleBed,
};

export const Default = () => <SingleBed />;
export const Fill = () => <SingleBed fill="blue" />;
export const Size = () => <SingleBed height="50" width="50" />;
export const CustomStyle = () => <SingleBed style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SingleBed className="custom-class" />;

export const Clickable = () => <SingleBed onClick={()=>console.log("clicked")} />;

const Template = (args) => <SingleBed {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
