import React from "react";
import { Polymer } from "./Polymer";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Polymer",
  component: Polymer,
};

export const Default = () => <Polymer />;
export const Fill = () => <Polymer fill="blue" />;
export const Size = () => <Polymer height="50" width="50" />;
export const CustomStyle = () => <Polymer style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Polymer className="custom-class" />;

export const Clickable = () => <Polymer onClick={()=>console.log("clicked")} />;

const Template = (args) => <Polymer {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
