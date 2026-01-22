import React from "react";
import { FlipToFront } from "./FlipToFront";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FlipToFront",
  component: FlipToFront,
};

export const Default = () => <FlipToFront />;
export const Fill = () => <FlipToFront fill="blue" />;
export const Size = () => <FlipToFront height="50" width="50" />;
export const CustomStyle = () => <FlipToFront style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FlipToFront className="custom-class" />;

export const Clickable = () => <FlipToFront onClick={()=>console.log("clicked")} />;

const Template = (args) => <FlipToFront {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
