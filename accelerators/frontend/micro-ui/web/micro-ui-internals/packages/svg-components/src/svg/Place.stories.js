import React from "react";
import { Place } from "./Place";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Place",
  component: Place,
};

export const Default = () => <Place />;
export const Fill = () => <Place fill="blue" />;
export const Size = () => <Place height="50" width="50" />;
export const CustomStyle = () => <Place style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Place className="custom-class" />;

export const Clickable = () => <Place onClick={()=>console.log("clicked")} />;

const Template = (args) => <Place {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
